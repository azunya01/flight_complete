# 飞行预订项目开发文档

> 本文档面向项目研发团队，重点解析数据库模型与核心业务流，帮助新成员快速理解系统结构并高效推进开发与运维工作。

## 1. 项目概览

- **项目名称**：Flight Complete 航班预订系统
- **主要目标**：提供航班检索、舱位库存维护、乘客管理与订单预订能力，支撑 C 端用户与后台管理功能。
- **技术栈**：
  - 后端：Java 17、Spring Boot、MyBatis、MySQL 8
  - 前端：独立 `frontend/` 模块（未在本文展开）
  - 构建工具：Maven（根目录 `pom.xml` 与各模块 `pom.xml`）

系统采用分层架构：Controller 提供 REST API，Service 实现业务逻辑，Mapper 基于 MyBatis 与数据库交互，POJO/VO/DTO 提供数据传输结构。数据库是业务核心，提供航班、座位、订单、乘客等实体信息。

## 2. 数据库设计

### 2.1 基础信息

- **数据库名**：`flight_booking`【F:schema.sql†L1-L24】
- **字符集**：`utf8mb4`，排序规则 `utf8mb4_general_ci`（部分表使用 `utf8mb4_0900_ai_ci`）。

### 2.2 核心实体与关系

| 实体 | 关键字段 | 关联/约束 | 说明 |
| --- | --- | --- | --- |
| `airport` | `id` PK，自增；`name`、`city`、`address`、`tel` | 无显式外键，业务通过 `airline.from_airport`、`airline.to_airport` 关联 | 机场信息字典表。【F:schema.sql†L45-L56】 |
| `airline` | `id` PK，自增；`from_airport`、`to_airport` | 与 `airport` 基于名称关联；`flight.airline_id` 外键引用 | 存储航线起止机场名称。【F:schema.sql†L27-L38】 |
| `plane` | `id` PK，自增；`name` | 被 `flight.plane_id` 引用 | 飞机机型或机号。【F:schema.sql†L168-L179】 |
| `seat_type` | `id` PK，自增；`name`、`code` 唯一 | 与座位、库存、容量表通过 `seat_type_id` 关联 | 定义舱位类型（经济舱/公务舱/头等舱等）。【F:schema.sql†L433-L446】 |
| `plane_seat_capacity` | 复合 PK (`plane_id`,`seat_type_id`)，`capacity` CHECK≥0 | 触发器维护 `plane_seat` 与 `flight_seat_inventory` | 定义不同机型在每种舱位的座位容量。【F:schema.sql†L200-L215】 |
| `plane_seat` | 复合 PK (`plane_id`,`seat_no`)，索引 (`plane_id`,`seat_type_id`) | 数据由触发器生成，反映机舱座位布局 | 座位明细，含行列号与舱位。【F:schema.sql†L182-L197】 |
| `flight` | `id` PK，自增；索引：`plane_id`、`airline_id`、`departure_time`、`name`; CHECK `arrival_time > departure_time` | 引用 `airline`、`plane`; 与库存、订单、航班查询核心关联 | 航班主数据。【F:schema.sql†L101-L121】 |
| `flight_seat_inventory` | 复合 PK (`flight_id`,`seat_type_id`)，索引 `seat_type_id`，CHECK 保证库存非负且不超总数 | 触发器与业务共同维护；与订单、库存扣减关系密切 | 航班-舱位库存与定价。【F:schema.sql†L123-L141】 |
| `booking_order` | `id` PK，自增；索引 `flight_id`、`status` | 与 `booking_order_item`、`passenger` 关联 | 用户订单头。【F:schema.sql†L58-L76】 |
| `booking_order_item` | `id` PK，自增；索引 `order_id`、`flight_id`、(`flight_id`,`seat_type_id`) | 记录乘客维度的订单明细 | 舱位票面价格、乘客关联。【F:schema.sql†L78-L98】 |
| `passenger` | `id` PK，自增；唯一约束 (`user_id`,`id_no`)；索引 `user_id`、`id_no` | 可独立于订单维护，也可在下单时同步 | 乘客档案，防重复建档。【F:schema.sql†L145-L164】 |
| `user` | `id` PK，自增；唯一约束 `name` | 区分普通用户/管理员（`type` 字段） | 登录账户。【F:schema.sql†L448-L463】 |

#### 2.2.1 关系拓扑要点

- **航班主线**：`flight` 同时关联 `airline` 与 `plane`，并在触发器作用下与 `flight_seat_inventory`、`plane_seat` 形成飞机-舱位-库存联动，构成售卖与运维的中心数据链路。【F:schema.sql†L101-L215】
- **订单主线**：`booking_order` ➝ `booking_order_item` ➝ `passenger` 构成一对多关系，`booking_order_item.flight_id` 直连 `flight` 便于按航班聚合查询，`passenger` 的唯一约束确保相同用户不会重复建档。【F:schema.sql†L58-L164】
- **字典维度**：`seat_type` 为所有舱位相关表提供标准化类型码；`airport` 以名称与航线做松耦合，留给后续国际化扩展空间。【F:schema.sql†L27-L140】【F:schema.sql†L433-L446】

### 2.3 触发器与自动同步

`plane_seat_capacity` 表定义了一组触发器，实现机型座位容量与航班舱位库存的联动：

- `trg_psc_ai`：新增容量记录时调用存储过程 `regen_plane_seats_for_type`，重建对应舱位的座位明细。【F:schema.sql†L224-L230】
- `trg_psc_after_insert`：自动为所有使用该机型的航班插入/更新 `flight_seat_inventory`，同步 `total`、`available` 与价格（沿用既有价格或默认 0）。【F:schema.sql†L243-L288】
- `trg_psc_au` 与 `trg_psc_after_update`：更新容量时先清理旧布局，再重建座位并批量更新航班库存，缺失记录会自动补齐，保证容量变更即时生效。【F:schema.sql†L303-L377】
- `trg_psc_ad` 与 `trg_psc_after_delete`：删除容量配置时清理座位表并将对应航班舱位库存置零，避免遗留库存。【F:schema.sql†L392-L425】

#### 2.3.1 触发器执行顺序与幂等性

1. 所有触发器均为 **AFTER** 触发，保证写入 `plane_seat_capacity` 成功后再做同步，避免因业务验证失败导致中间态脏数据。
2. `regen_plane_seats_for_type` 每次执行都会先删除旧座位再批量插入新布局，依赖主键 (`plane_id`,`seat_no`) 保证生成结果幂等；航班库存同步采用 `INSERT ... ON DUPLICATE KEY UPDATE`，确保重复执行不会产生多余行。【F:schema.sql†L236-L288】
3. 若在同一事务内多次调整同一舱位容量，触发器会按顺序执行，最终以最后一次写入为准，适用于管理端多次修改场景。

### 2.4 索引策略

- 航班表围绕常用查询条件（飞机、航线、起飞时间、航班号）建立索引，支持列表与检索性能。【F:schema.sql†L114-L119】
- 库存表使用复合主键与舱位索引以支撑扣减和聚合。【F:schema.sql†L136-L140】
- 订单、乘客、座位等表均配置了针对查询场景的索引（如订单状态筛选、乘客唯一校验）。【F:schema.sql†L72-L97】【F:schema.sql†L160-L163】

### 2.5 存储过程与批处理逻辑

- `regen_plane_seats_for_type(p_plane_id, p_seat_type_id)`：读取 `plane_seat_capacity` 与 `seat_type` 获取容量、每行座位数、舱位代码，清空旧座位后按 1..capacity 生成 `seat_no`（如 `Y-01`）。循环逻辑保证不同舱位可独立重建，`row_number` 默认为 1，空值自动按单列布局处理。【F:CompletelyCreate.sql†L560-L658】
- `regen_plane_seats_all(p_plane_id)`：游标遍历某机型的全部舱位配置，批量调用 `regen_plane_seats_for_type`，用于批量初始化或修复整架飞机座位模板。【F:CompletelyCreate.sql†L664-L706】
- 管理端脚本可在更新舱位配置后调用 `CALL regen_plane_seats_all(planeId);` 作为幂等操作，确保座位模板与容量表一致。

### 2.6 数据字典（字段级说明）

> 以下字段描述基于当前 `schema.sql` 及 `CompletelyCreate.sql`，若数据库迁移需同步更新。

#### 2.6.1 `airline`

- `id` INT PK，自增。
- `from_airport` VARCHAR(50)：始发机场名称。
- `to_airport` VARCHAR(50)：到达机场名称。【F:schema.sql†L27-L38】

#### 2.6.2 `airport`

- `id` INT PK，自增。
- `name` VARCHAR(50)：机场名称。
- `city` VARCHAR(50)：城市。
- `address` VARCHAR(50)：地址。
- `tel` VARCHAR(50)：客服电话。【F:schema.sql†L40-L56】

#### 2.6.3 `booking_order`

- `id` BIGINT PK，自增。
- `user_id` VARCHAR(64)：下单用户 ID。
- `flight_id` INT：关联航班。
- `status` TINYINT：订单状态（1=已确认，2=已取消，3=已完成可扩展）。
- `total_amount` DECIMAL(10,2)：订单总额。
- `created_at` DATETIME：创建时间，默认当前时间戳。【F:schema.sql†L58-L76】

#### 2.6.4 `booking_order_item`

- `id` BIGINT PK，自增。
- `order_id` BIGINT：订单头主键。
- `flight_id` INT：冗余航班 ID，便于按航班统计。
- `seat_type_id` TINYINT：舱位类型。
- `price` DECIMAL(10,2)：单张票价。
- `created_at` DATETIME：创建时间。
- `passenger_id` INT：关联乘客。【F:schema.sql†L78-L98】

#### 2.6.5 `flight`

- `id` INT PK，自增。
- `name` VARCHAR(50)：航班号。
- `plane_id` INT：关联飞机。
- `airline_id` INT：关联航线。
- `departure_time` DATETIME：起飞时间。
- `arrival_time` DATETIME：到达时间（CHECK 约束 > 起飞时间）。【F:schema.sql†L101-L121】

#### 2.6.6 `flight_seat_inventory`

- `flight_id` INT：航班主键。
- `seat_type_id` TINYINT：舱位类型。
- `total` INT：可售总量。
- `available` INT：剩余量。
- `price` DECIMAL(10,2)：当前对外售卖价。【F:schema.sql†L123-L141】

#### 2.6.7 `passenger`

- `id` BIGINT PK，自增。
- `user_id` VARCHAR(64)：所属用户，可为空。
- `name` VARCHAR(50)：乘客姓名。
- `gender` CHAR(1)：性别。
- `id_no` VARCHAR(32)：证件号，和 `user_id` 组合唯一。
- `phone` VARCHAR(20)：联系电话。
- `created_at` DATETIME：创建时间。
- `updated_at` DATETIME：最近更新时间。【F:schema.sql†L145-L164】

#### 2.6.8 `plane`

- `id` INT PK，自增。
- `name` VARCHAR(50)：机型或机号。【F:schema.sql†L168-L179】

#### 2.6.9 `plane_seat`

- `plane_id` INT：飞机主键。
- `seat_no` VARCHAR(8)：座位号，格式 `舱位码-序号`。
- `seat_type_id` TINYINT：舱位类型。
- `row_no` SMALLINT：行号。
- `seat_col` CHAR(1)：列序号，按 `row_number` 配置依次生成（1..N）。【F:schema.sql†L182-L197】【F:CompletelyCreate.sql†L598-L638】

#### 2.6.10 `plane_seat_capacity`

- `plane_id` INT：飞机主键。
- `seat_type_id` INT：舱位类型。
- `capacity` INT：该舱位总座位数。
- `row_number` INT：每行座位数（用于生成 `row_no` 与 `seat_col`）。【F:schema.sql†L200-L215】【F:CompletelyCreate.sql†L572-L621】

#### 2.6.11 `seat_type`

- `id` INT PK，自增。
- `name` VARCHAR(20)：舱位名称。
- `code` VARCHAR(10)：舱位编码，唯一索引保证全局唯一。【F:schema.sql†L433-L446】

#### 2.6.12 `user`

- `id` INT PK，自增。
- `name` VARCHAR(50)：登录名（唯一）。
- `password` VARCHAR(50)：加密密码。
- `type` INT：账户类型（0=普通用户，1=管理员可扩展）。【F:schema.sql†L448-L463】

### 2.7 数据完整性与校验策略

- **唯一性与幂等**：`passenger` 表通过联合唯一索引 `uk_passenger_user_doc` 杜绝重复证件档案，`seat_type.code` 独占舱位编码，确保字典取值稳定。【F:schema.sql†L161-L164】【F:schema.sql†L433-L446】
- **库存数值约束**：`flight_seat_inventory` 定义多重 CHECK 约束限制 `total`、`available` 非负且不超过总量，为事务扣减提供底线保障。【F:schema.sql†L130-L140】
- **时间一致性**：`flight` 表约束 `arrival_time > departure_time`，保证航班计划逻辑正确；业务端无需重复校验，可直接依赖数据库失败回滚。【F:schema.sql†L107-L120】
- **触发器协同**：所有与 `plane_seat_capacity` 相关的触发器都在 AFTER 阶段触发，结合 `INSERT ... ON DUPLICATE KEY UPDATE` 实现幂等写入，任何失败都会回滚至原状态。【F:schema.sql†L224-L377】

## 3. 核心业务流程与数据库交互

### 3.1 航班搜索

- Mapper：`UserFlightSearchMapper`。
- 查询逻辑：根据起降城市与日期区间筛选航班，聚合最低票价与剩余座位数，分页输出。【F:sky-server/src/main/resources/mapper/UserFlightSearchMapper.xml†L18-L70】
- 计数与分页均通过 `flight`、`airline`、`airport`、`flight_seat_inventory` 关联完成，保证搜索结果与库存同步。

### 3.2 航班详情与舱位列表

- Mapper：`UserBookMapper.getFlightDetailById` 查询航班基本信息，并嵌套 `getSeatTypesByFlightId` 返回舱位价与余票信息。【F:sky-server/src/main/resources/mapper/UserBookMapper.xml†L7-L65】
- 舱位列表基于 `flight_seat_inventory` + `seat_type` 聚合，计算 `haveAvailable` 标记，支持前端展示可售状态。【F:sky-server/src/main/resources/mapper/UserBookMapper.xml†L52-L65】

### 3.3 乘客档案复用

- `findPassengerIdByUserAndIdNo` 通过用户与证件号查询乘客；不存在时调用 `insertPassengerFromSnapshot` 复制行程单信息，依赖乘客表唯一约束避免重复。【F:sky-server/src/main/resources/mapper/UserBookMapper.xml†L122-L135】【F:schema.sql†L160-L164】

### 3.4 下单与库存扣减

1. **库存锁定**：`decreaseAvailable` 使用条件更新在数据库层面原子扣减 `available`，同时校验剩余数量，避免超卖。【F:sky-server/src/main/resources/mapper/UserBookMapper.xml†L87-L93】【F:schema.sql†L123-L140】
2. **订单创建**：
   - `insertOrder` 将订单头写入 `booking_order`，记录用户、航班、订单状态、总金额。【F:sky-server/src/main/resources/mapper/UserBookMapper.xml†L95-L98】【F:schema.sql†L65-L75】
   - `insertOrderItems` 批量写入乘客维度的舱位与价格信息。【F:sky-server/src/main/resources/mapper/UserBookMapper.xml†L104-L110】【F:schema.sql†L85-L97】
   - `lastInsertId` 获取订单主键用于明细插入。【F:sky-server/src/main/resources/mapper/UserBookMapper.xml†L100-L102】
3. **事务建议**：在 Service 层通过 Spring 事务注解包裹扣减库存、创建订单头与明细，确保失败回滚。

### 3.5 座位容量维护

- 管理端修改 `plane_seat_capacity` 后，由数据库触发器自动更新 `plane_seat` 与 `flight_seat_inventory`，确保前台库存感知实时变更。【F:schema.sql†L243-L377】
- 若需要调整价格，可直接更新 `flight_seat_inventory.price`；后续容量调整会保留最近一次非零价格（触发器通过 `COALESCE` 读取历史价格）。【F:schema.sql†L253-L278】

### 3.6 订单取消与售后回补

1. **业务入口**：`UserOrderService.cancelOrder` 先检查订单状态，仅对状态 `1`（已确认/待出行）执行库存回补，其他状态直接更新为取消，避免重复返库存。【F:sky-server/src/main/java/com/sky/service/UserOrderService.java†L19-L44】
2. **库存回补**：通过 `UserOrderMapper.listSeatTypeIds` 获取该订单占用的舱位列表，逐一调用 `increaseAvailable` 将 `flight_seat_inventory.available` +1，确保数据库层面原子恢复库存。【F:sky-server/src/main/resources/mapper/UserOrderMapper.xml†L9-L21】
3. **状态流转**：最终调用 `updateOrderStatus` 将订单标记为 2（已取消），保留订单记录以供审计与报表分析。【F:sky-server/src/main/resources/mapper/UserOrderMapper.xml†L23-L27】

### 3.7 航班舱位配置全链路

1. **新增机型或舱位**：写入 `plane`、`seat_type` 后，在 `plane_seat_capacity` 新增容量与排布信息；触发器会自动重建 `plane_seat` 并为所有使用该机型的航班生成初始库存。【F:schema.sql†L168-L288】
2. **批量修复**：若发现座位模板缺失，可手动执行 `CALL regen_plane_seats_all(planeId);` 或对 `plane_seat_capacity` 做一次空更新触发重建；适用于脚本导入或手工修复场景。【F:CompletelyCreate.sql†L664-L706】
3. **航班层库存配置**：创建航班后，通过管理端界面或 SQL 更新 `flight_seat_inventory.price`，并在上线前核对 `total/available` 是否与容量匹配；如需差异化库存（如预留），业务层可在 `available` 上做扣减但保留 `total` 以便后续还原。

### 3.8 数据分析与常用报表 SQL

- **航班销售进度**：`SELECT flight_id, seat_type_id, total, total-available AS sold, price FROM flight_seat_inventory WHERE flight_id = ?;` 快速查看单航班各舱位售卖情况，可用于后台仪表盘。
- **用户订单概览**：`SELECT user_id, COUNT(*) AS orders, SUM(total_amount) AS revenue FROM booking_order GROUP BY user_id;` 结合应用服务分页接口可构建用户画像。
- **乘客复用率**：`SELECT user_id, COUNT(DISTINCT id_no) AS passengers FROM passenger GROUP BY user_id;` 观测乘客档案覆盖度，为产品优化提供依据。

### 3.9 Java 层数据库操作实现

- **搜索与航班详情聚合**：`UserFlightSearchMapper`、`UserBookMapper` 通过 MyBatis XML Mapper 将航班、机场与库存表的多表 join 封装成 SQL 语句，Service 端直接以 VO 返回分页结果与舱位详情，避免在业务层手写 SQL。【F:sky-server/src/main/resources/mapper/UserFlightSearchMapper.xml†L1-L70】【F:sky-server/src/main/resources/mapper/UserBookMapper.xml†L1-L136】
- **下单流程编排**：`UserBookService.book` 在单个事务中执行乘客建档、舱位库存扣减、订单头/明细写入与回显，保证数据库状态一致；该方法优先判断乘客是否已存在，若缺失则调用 Mapper 新建记录并继续下单。【F:sky-server/src/main/java/com/sky/service/UserBookService.java†L21-L103】【F:sky-server/src/main/resources/mapper/UserBookMapper.xml†L67-L135】
- **订单取消与回补**：`UserOrderService.cancelOrder` 以 `@Transactional` 包裹取数、库存回补与状态更新，具体的库存增量 SQL、订单状态写入由 `UserOrderMapper` 完成，确保数据库层面原子恢复库存。【F:sky-server/src/main/java/com/sky/service/UserOrderService.java†L17-L78】【F:sky-server/src/main/resources/mapper/UserOrderMapper.xml†L7-L138】
- **分页查询复用**：订单列表查询在 Service 内负责分页参数与乘客聚合，MyBatis Mapper 侧完成统计与订单头分页 SQL，从而复用同一 Mapper 满足接口与报表需求。【F:sky-server/src/main/java/com/sky/service/UserOrderService.java†L45-L78】【F:sky-server/src/main/resources/mapper/UserOrderMapper.xml†L74-L138】
- **常用乘客维护**：`UserPassengerService` 在新增时捕获 `DuplicateKeyException` 以提示证件号冲突，更新、删除、查询均附带 `userId` 校验，Mapper 统一封装分页搜索、唯一键回查等 SQL，保证同一用户的数据隔离。【F:sky-server/src/main/java/com/sky/service/UserPassengerService.java†L18-L59】【F:sky-server/src/main/resources/mapper/UserPassengerMapper.xml†L7-L77】

### 3.10 主要 API 调用流程

> 以下流程以用户侧接口为例，串联 **Controller → Service → Mapper → 数据表** 的调用路径，帮助理解一次请求在后端与数据库之间的传递关系。

#### 3.10.1 航班搜索与城市提示

1. **请求入口**：前端通过 `GET /user/flights/search` 传入出发地、目的地、出行日期及分页参数，`UserFlightSearchController.search` 接收后委托 Service 处理并返回分页结果。【F:sky-server/src/main/java/com/sky/controller/UserFlightSearchController.java†L26-L44】
2. **业务层处理**：`UserFlightSearchService.search` 负责计算分页偏移量、构造当天的起止时间窗口，并调用 Mapper 分别获取总数和列表数据。【F:sky-server/src/main/java/com/sky/service/UserFlightSearchService.java†L22-L39】
3. **数据库交互**：`UserFlightSearchMapper` 中的 `countByCitiesAndDate` 与 `selectByCitiesAndDate` SQL 关联 `flight`、`airline`、`airport`、`flight_seat_inventory` 等表，统计满足条件的航班数量并聚合舱位价格与余票总数。【F:sky-server/src/main/resources/mapper/UserFlightSearchMapper.xml†L18-L59】
4. **城市联想**：若需要城市下拉提示，前端调用 `GET /user/flights/cities`，Controller 直接转发到 Service，再由 Mapper 从 `airport` 表去重城市名称并支持关键字模糊匹配。【F:sky-server/src/main/java/com/sky/controller/UserFlightSearchController.java†L40-L44】【F:sky-server/src/main/java/com/sky/service/UserFlightSearchService.java†L41-L43】【F:sky-server/src/main/resources/mapper/UserFlightSearchMapper.xml†L61-L70】

#### 3.10.2 航班详情查询与下单

1. **航班详情**：`GET /user/book?flightId=` 由 `UserBookController.detail` 转发到 `UserBookService.detail`，后者调用 `UserBookMapper.getFlightDetailById` 和嵌套查询 `getSeatTypesByFlightId` 返回航班基础信息、舱位价格及余票状态，数据分别来自 `flight`、`airline`、`airport`、`flight_seat_inventory`、`seat_type` 表。【F:sky-server/src/main/java/com/sky/controller/UserBookController.java†L23-L31】【F:sky-server/src/main/java/com/sky/service/UserBookService.java†L24-L27】【F:sky-server/src/main/resources/mapper/UserBookMapper.xml†L23-L65】
2. **创建订单**：`POST /user/book` 请求体包含航班 ID、舱位、乘客信息，`UserBookController.book` 将当前登录用户写入 DTO 后交给 Service。【F:sky-server/src/main/java/com/sky/controller/UserBookController.java†L33-L38】
3. **事务编排**：`UserBookService.book` 在事务中依次执行乘客档案查建（`passenger` 表）、舱位库存扣减（更新 `flight_seat_inventory.available`）、写入订单头 `booking_order` 与订单明细 `booking_order_item`，最后回查乘客信息封装响应。【F:sky-server/src/main/java/com/sky/service/UserBookService.java†L31-L87】【F:sky-server/src/main/resources/mapper/UserBookMapper.xml†L67-L136】
4. **库存与价格确认**：若前端未指定舱位，Service 会列出全部可售舱位选项并择优选择可满足人数的最优价舱位，再调用 `decreaseAvailable` 做行级条件更新，确保库存扣减不会超卖。【F:sky-server/src/main/java/com/sky/service/UserBookService.java†L51-L75】【F:sky-server/src/main/resources/mapper/UserBookMapper.xml†L71-L110】

#### 3.10.3 订单列表与取消流程

1. **订单分页**：`GET /user/orders/list` 在 `UserOrderController.list` 中读取当前用户并调用 `UserOrderService.listOrders`。【F:sky-server/src/main/java/com/sky/controller/UserOrderController.java†L21-L34】
2. **聚合逻辑**：Service 计算分页范围后，通过 Mapper 查询订单总数、分页订单头，再批量拉取乘客并按订单聚合，最终返回包含乘客列表的 `PageResult`。【F:sky-server/src/main/java/com/sky/service/UserOrderService.java†L53-L92】【F:sky-server/src/main/resources/mapper/UserOrderMapper.xml†L33-L138】
3. **取消订单**：`POST /user/orders/{orderId}/cancel` 由 Controller 调用 `UserOrderService.cancelOrder`，Service 先读取订单状态，针对已确认订单循环回补 `flight_seat_inventory.available`，最后更新 `booking_order.status` 为取消。【F:sky-server/src/main/java/com/sky/controller/UserOrderController.java†L36-L42】【F:sky-server/src/main/java/com/sky/service/UserOrderService.java†L20-L46】【F:sky-server/src/main/resources/mapper/UserOrderMapper.xml†L7-L29】

#### 3.10.4 常用乘客管理

1. **新增/更新/删除**：`UserPassengerController` 暴露 `POST /user/passengers`、`PUT /user/passengers/{id}`、`DELETE /user/passengers/{id}` 等接口，统一从登录上下文写入 `userId`，并交由 `UserPassengerService` 执行写库操作。【F:sky-server/src/main/java/com/sky/controller/UserPassengerController.java†L18-L45】
2. **Service 行为**：`UserPassengerService` 调用 Mapper 将请求映射为 `passenger` 表的插入、更新、删除，并在更新/删除失败时抛出异常提示归属校验未通过。【F:sky-server/src/main/java/com/sky/service/UserPassengerService.java†L18-L43】【F:sky-server/src/main/resources/mapper/UserPassengerMapper.xml†L14-L33】
3. **查询能力**：列表接口 `GET /user/passengers` 支持关键字搜索和分页，Mapper 对 `passenger` 表做模糊匹配；单个详情和证件号查重接口则使用主键与唯一键组合保障只访问当前用户的数据。【F:sky-server/src/main/java/com/sky/controller/UserPassengerController.java†L47-L64】【F:sky-server/src/main/java/com/sky/service/UserPassengerService.java†L45-L58】【F:sky-server/src/main/resources/mapper/UserPassengerMapper.xml†L34-L78】


### 3.11 Java 接口与数据库操作矩阵

| 功能模块 | HTTP 方法 / 路径 | Controller 入口 | Service 核心逻辑 | Mapper / SQL | 涉及数据表 |
| --- | --- | --- | --- | --- | --- |
| 航班搜索 | `GET /user/flights/search` | `UserFlightSearchController.search` 负责参数校验与结果封装。【F:sky-server/src/main/java/com/sky/controller/UserFlightSearchController.java†L26-L43】 | `UserFlightSearchService.search` 计算分页窗口并调用 Mapper 统计/查询。【F:sky-server/src/main/java/com/sky/service/UserFlightSearchService.java†L22-L43】 | `UserFlightSearchMapper.countByCitiesAndDate` / `selectByCitiesAndDate` 做多表 join 并聚合余票、最低价。【F:sky-server/src/main/resources/mapper/UserFlightSearchMapper.xml†L7-L70】 | `flight`、`airline`、`airport`、`flight_seat_inventory` |
| 航班详情 | `GET /user/book?flightId=` | `UserBookController.detail` 读取航班编号后返回详情。【F:sky-server/src/main/java/com/sky/controller/UserBookController.java†L25-L31】 | `UserBookService.detail` 直接调 Mapper 回传航班基础信息与舱位列表。【F:sky-server/src/main/java/com/sky/service/UserBookService.java†L25-L27】 | `UserBookMapper.getFlightDetailById` 嵌套 `getSeatTypesByFlightId`，组合航班信息与舱位可售状态。【F:sky-server/src/main/resources/mapper/UserBookMapper.xml†L8-L65】 | `flight`、`airline`、`airport`、`flight_seat_inventory`、`seat_type` |
| 创建订单 | `POST /user/book` | `UserBookController.book` 注入当前用户并触发下单流程。【F:sky-server/src/main/java/com/sky/controller/UserBookController.java†L33-L40】 | `UserBookService.book` 按“乘客建档 → 库存扣减 → 写订单头/明细 → 回显”顺序执行事务。【F:sky-server/src/main/java/com/sky/service/UserBookService.java†L29-L103】 | `UserBookMapper` 提供乘客查建、库存扣减、订单写入等 SQL。【F:sky-server/src/main/resources/mapper/UserBookMapper.xml†L67-L135】 | `passenger`、`flight_seat_inventory`、`booking_order`、`booking_order_item` |
| 订单分页 | `GET /user/orders/list` | `UserOrderController.list` 拉取当前用户订单列表。【F:sky-server/src/main/java/com/sky/controller/UserOrderController.java†L24-L34】 | `UserOrderService.listOrders` 组合分页、批量查询乘客并组装 VO。【F:sky-server/src/main/java/com/sky/service/UserOrderService.java†L45-L78】 | `UserOrderMapper` 提供总数、分页订单头及乘客聚合 SQL。【F:sky-server/src/main/resources/mapper/UserOrderMapper.xml†L74-L138】 | `booking_order`、`booking_order_item`、`flight`、`airline`、`passenger`、`seat_type` |
| 订单取消 | `POST /user/orders/{id}/cancel` | `UserOrderController.cancel` 触发取消操作。【F:sky-server/src/main/java/com/sky/controller/UserOrderController.java†L37-L40】 | `UserOrderService.cancelOrder` 读取订单状态、回补库存并更新状态。【F:sky-server/src/main/java/com/sky/service/UserOrderService.java†L27-L42】 | `UserOrderMapper` 提供订单基础信息、舱位列表与库存回补 SQL。【F:sky-server/src/main/resources/mapper/UserOrderMapper.xml†L7-L29】 | `booking_order`、`booking_order_item`、`flight_seat_inventory` |
| 常用乘客管理 | `POST/PUT/DELETE/GET /user/passengers` | `UserPassengerController` 将用户上下文写入 DTO 并路由到 Service。【F:sky-server/src/main/java/com/sky/controller/UserPassengerController.java†L25-L67】 | `UserPassengerService` 处理新增防重、归属校验、分页查询等逻辑。【F:sky-server/src/main/java/com/sky/service/UserPassengerService.java†L22-L59】 | `UserPassengerMapper` 负责 CRUD、唯一键查询及分页模糊搜索。【F:sky-server/src/main/resources/mapper/UserPassengerMapper.xml†L14-L77】 | `passenger` |

### 3.12 DTO / VO 数据结构与映射

- **下单输入**：`BookDTO` 承载用户 ID、航班 ID、指定舱位及乘客列表；每个 `PassengerDTO` 携带乘客主键或三要素（姓名、证件号、电话）用于自动建档。【F:sky-pojo/src/main/java/com/sky/dto/BookDTO.java†L1-L12】【F:sky-pojo/src/main/java/com/sky/dto/PassengerDTO.java†L1-L12】
- **乘客写入**：`PassengerCreateDTO`、`PassengerUpdateDTO` 均包含 `userId` 字段，Service 会使用它与主键联合校验，防止跨用户操作他人乘客记录。【F:sky-pojo/src/main/java/com/sky/dto/PassengerCreateDTO.java†L1-L12】【F:sky-pojo/src/main/java/com/sky/dto/PassengerUpdateDTO.java†L1-L12】
- **航班视图对象**：`UserFlightVO`、`UserFlightDetailVO` 与 `UserSeatTypeVO` 对应航班列表、详情及舱位余票视图，结合 MyBatis 结果映射直接序列化给前端，无需额外转换。【F:sky-pojo/src/main/java/com/sky/vo/UserFlightVO.java†L1-L19】【F:sky-pojo/src/main/java/com/sky/vo/UserFlightDetailVO.java†L1-L19】【F:sky-pojo/src/main/java/com/sky/vo/UserSeatTypeVO.java†L1-L11】
- **下单返回**：`BookVO` 聚合订单号、航班名、总价与乘客列表，生成于 Service 层（不依赖数据库视图）用于展示下单结果。【F:sky-pojo/src/main/java/com/sky/vo/BookVO.java†L1-L16】
- **订单与乘客视图**：`OrderVO` 携带航班时间、舱位、金额、状态，并嵌套 `PassengerVO` 列表；后者映射乘客主键与基础属性，匹配 `UserOrderMapper` 与 `UserPassengerMapper` 的 resultMap 定义。【F:sky-pojo/src/main/java/com/sky/vo/OrderVO.java†L1-L20】【F:sky-pojo/src/main/java/com/sky/vo/PassengerVO.java†L1-L11】


## 4. 开发与运维指引

### 4.1 初始化数据库

1. 安装 MySQL 8.x，并创建名为 `flight_booking` 的数据库。
2. 执行 `schema.sql` 导入结构与触发器（包含数据库创建语句，可直接运行）。
3. 若需要基础数据，可参考 `CompletelyCreate.sql`（包含更多初始化脚本）。

### 4.2 本地 Java 服务开发与运行

1. **模块依赖**：根 `pom.xml` 声明了 Java 17 与多模块结构，`sky-server` 作为运行入口依赖 `sky-common`、`sky-pojo`，并引入 Spring Boot、MyBatis、Redis、Druid 等组件；Spring Boot Maven 插件负责打包与运行生命周期。【F:pom.xml†L7-L147】【F:sky-server/pom.xml†L5-L128】
2. **配置管理**：默认激活 `dev` Profile，连接信息通过 `sky.*` 自定义配置项注入 Druid 数据源与 Redis；复制 `application-dev.yml.example` 为 `application-dev.yml` 后按本地环境修改数据库与 Redis 连接参数即可运行。【F:sky-server/src/main/resources/application.yml†L4-L53】【F:sky-server/src/main/resources/application-dev.yml.example†L1-L13】
3. **启动方式**：`SkyApplication` 为 Spring Boot 主类，已启用事务与缓存。开发阶段可执行 `mvn -pl sky-server spring-boot:run` 或在 IDE 直接运行 `SkyApplication.main`；打包发布可执行 `mvn -pl sky-server -am clean package` 生成可执行 JAR。【F:sky-server/src/main/java/com/sky/SkyApplication.java†L1-L18】【F:sky-server/pom.xml†L121-L126】
4. **事务与数据库操作约定**：所有涉及多表写入的 Service（如下单、取消订单）均使用 `@Transactional`，Mapper 层集中存放 SQL，避免在 Service 直接拼接语句；新增数据库操作时优先扩展 Mapper XML，保持与现有结构一致。【F:sky-server/src/main/java/com/sky/service/UserBookService.java†L29-L103】【F:sky-server/src/main/java/com/sky/service/UserOrderService.java†L27-L78】【F:sky-server/src/main/resources/mapper/UserBookMapper.xml†L87-L135】【F:sky-server/src/main/resources/mapper/UserOrderMapper.xml†L7-L138】
5. **调试建议**：利用 `application.yml` 中的日志级别配置，可在开发阶段开启 Mapper SQL 日志与 DispatcherServlet 调用链路，便于排查数据库交互问题；如需对特定 Mapper 调整级别，可在 Profile 配置中覆写日志设置。【F:sky-server/src/main/resources/application.yml†L33-L41】
6. **MyBatis 设置**：`mapper-locations` 指向 `classpath:mapper/*.xml`，`type-aliases-package` 与驼峰映射配置让 XML 结果自动绑定至 Java VO/Entity，减少手工映射代码量。【F:sky-server/src/main/resources/application.yml†L24-L31】

### 4.3 数据一致性与调试建议

- **并发预订**：确保所有扣减库存逻辑走数据库条件更新或存储过程，禁止在内存计算后回写。
- **事务边界**：跨表写入（库存、订单、乘客）必须放在单个事务内，以防部分成功。
- **触发器调试**：
  - 当库存出现不一致，重点检查 `plane_seat_capacity` 是否正确维护；可通过手动执行 `CALL regen_plane_seats_for_type(planeId, seatTypeId);` 重新生成座位。
  - 更新/删除容量后若发现航班库存未同步，确认 MySQL 触发器是否成功导入（`SHOW TRIGGERS LIKE 'plane_seat_capacity';`）。

### 4.4 数据迁移与版本控制

1. **结构变更**：推荐通过 Liquibase/Flyway 等迁移工具编写脚本，并在迁移中显式处理触发器与存储过程的创建顺序，避免因依赖缺失导致导入失败。
2. **数据回填**：新增字段需提供默认值或回填脚本，尤其是与库存相关的数值字段（`total`、`available`）必须保持非负，避免触发 CHECK 约束失败。【F:schema.sql†L136-L140】
3. **测试环境同步**：对触发器、存储过程等非表对象，迁移脚本需包含 `DROP ... IF EXISTS` 以保证环境一致性；导出数据库时建议使用 `mysqldump --routines` 保留存储过程定义。

### 4.5 备份、监控与性能调优

- **备份策略**：对核心库启用每日全量备份 + 小时级 binlog，确保库存与订单数据可回溯；在恢复后需执行 `CALL regen_plane_seats_all(planeId);` 以防座位模板缺失。【F:CompletelyCreate.sql†L664-L706】
- **监控指标**：重点关注 `flight_seat_inventory.available` 是否出现负数（理论不应发生），可通过数据库 CHECK 约束与业务日志双重监控。【F:schema.sql†L136-L140】
- **性能建议**：航班查询类 SQL 可结合 `EXPLAIN` 验证索引使用情况，必要时新增覆盖索引；订单类查询需限制分页大小，避免 `OFFSET` 过大导致性能下降（服务层已将 size 限制在 100 以内）。【F:sky-server/src/main/java/com/sky/service/UserOrderService.java†L47-L60】

### 4.6 扩展方向

- **国际化机场映射**：当前 `airline` 通过机场名称关联，可引入外键或机场编码增强约束。
- **价格历史**：在 `flight_seat_inventory` 增加价格变更历史表或审计字段。
- **库存分仓**：若未来需要区分代理渠道，可在库存表增加渠道维度或引入库存分表。

## 5. 常见问题

1. **触发器导致批量导入缓慢**：可在批量导入前暂时禁用触发器或分批处理，完成后重新启用并执行重建存储过程。
2. **航班删除逻辑**：若删除 `flight`，需要同步清理 `flight_seat_inventory`、`booking_order`、`booking_order_item` 中关联数据；建议新增级联处理或逻辑删除字段。
3. **乘客证件冲突**：唯一约束确保单用户不会重复录入同一证件，如需跨用户合并需调整索引策略。【F:schema.sql†L160-L164】

## 6. 数据安全与权限管理

### 6.1 账户角色模型

- `user.type` 字段区分用户类型（如普通用户/管理员），配合业务逻辑控制后端接口暴露范围。【F:schema.sql†L448-L463】
- `booking_order.user_id` 与 `passenger.user_id` 采用 VARCHAR 字段承载账号标识，虽然未设置外键，但在服务层应保持与 `user.id` 的一致性，避免孤儿数据。【F:schema.sql†L58-L164】

### 6.2 敏感数据保护

- 乘客表中的 `id_no`、`phone` 等字段属于敏感信息，建议在应用层进行脱敏展示，并在日志打印时截断或掩码处理。【F:schema.sql†L145-L164】
- 订单表的 `total_amount`、库存表的 `price` 等财务数据需限制修改来源，可通过数据库账户权限或存储过程封装写入逻辑保障合规性。【F:schema.sql†L65-L141】

### 6.3 权限与审计建议

1. **数据库账户分级**：至少区分读写账户与只读账户，授予最小权限；运维脚本可使用只读账号执行报表查询。
2. **操作审计**：对涉及 `plane_seat_capacity`、`flight_seat_inventory` 的变更操作，建议在业务层记录操作人、时间和变更前后数据快照，以便追溯库存异常。【F:schema.sql†L123-L377】
3. **定期巡检**：每周检查是否存在 `flight_seat_inventory.available > total`、`booking_order.user_id` 为空等违规数据，可通过 SQL 监控脚本实现。

---

如需进一步了解具体 API 或前端调用流程，可结合 `sky-server` 下 Controller、Service、Mapper 源码阅读。本文档将持续更新，欢迎在代码仓库提交改进建议。
