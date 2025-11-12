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

### 2.3 触发器与自动同步

`plane_seat_capacity` 表定义了一组触发器，实现机型座位容量与航班舱位库存的联动：

- `trg_psc_ai`：新增容量记录时调用存储过程 `regen_plane_seats_for_type`，重建对应舱位的座位明细。【F:schema.sql†L224-L230】
- `trg_psc_after_insert`：自动为所有使用该机型的航班插入/更新 `flight_seat_inventory`，同步 `total`、`available` 与价格（沿用既有价格或默认 0）。【F:schema.sql†L243-L288】
- `trg_psc_au` 与 `trg_psc_after_update`：更新容量时先清理旧布局，再重建座位并批量更新航班库存，缺失记录会自动补齐，保证容量变更即时生效。【F:schema.sql†L303-L377】
- `trg_psc_ad` 与 `trg_psc_after_delete`：删除容量配置时清理座位表并将对应航班舱位库存置零，避免遗留库存。【F:schema.sql†L392-L425】

### 2.4 索引策略

- 航班表围绕常用查询条件（飞机、航线、起飞时间、航班号）建立索引，支持列表与检索性能。【F:schema.sql†L114-L119】
- 库存表使用复合主键与舱位索引以支撑扣减和聚合。【F:schema.sql†L136-L140】
- 订单、乘客、座位等表均配置了针对查询场景的索引（如订单状态筛选、乘客唯一校验）。【F:schema.sql†L72-L97】【F:schema.sql†L160-L163】

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

## 4. 开发与运维指引

### 4.1 初始化数据库

1. 安装 MySQL 8.x，并创建名为 `flight_booking` 的数据库。
2. 执行 `schema.sql` 导入结构与触发器（包含数据库创建语句，可直接运行）。
3. 若需要基础数据，可参考 `CompletelyCreate.sql`（包含更多初始化脚本）。

### 4.2 本地开发环境

1. 导入 Maven 项目：`sky-common`、`sky-pojo`、`sky-server`。
2. 在 `sky-server/src/main/resources/application.yml`（或同目录配置文件）中设置数据库连接（用户名、密码、URL）。
3. 启动 `sky-server` Spring Boot 应用，前端模块视需求启动。

### 4.3 数据一致性与调试建议

- **并发预订**：确保所有扣减库存逻辑走数据库条件更新或存储过程，禁止在内存计算后回写。
- **事务边界**：跨表写入（库存、订单、乘客）必须放在单个事务内，以防部分成功。
- **触发器调试**：
  - 当库存出现不一致，重点检查 `plane_seat_capacity` 是否正确维护；可通过手动执行 `CALL regen_plane_seats_for_type(planeId, seatTypeId);` 重新生成座位。
  - 更新/删除容量后若发现航班库存未同步，确认 MySQL 触发器是否成功导入（`SHOW TRIGGERS LIKE 'plane_seat_capacity';`）。

### 4.4 扩展方向

- **国际化机场映射**：当前 `airline` 通过机场名称关联，可引入外键或机场编码增强约束。
- **价格历史**：在 `flight_seat_inventory` 增加价格变更历史表或审计字段。
- **库存分仓**：若未来需要区分代理渠道，可在库存表增加渠道维度或引入库存分表。

## 5. 常见问题

1. **触发器导致批量导入缓慢**：可在批量导入前暂时禁用触发器或分批处理，完成后重新启用并执行重建存储过程。
2. **航班删除逻辑**：若删除 `flight`，需要同步清理 `flight_seat_inventory`、`booking_order`、`booking_order_item` 中关联数据；建议新增级联处理或逻辑删除字段。
3. **乘客证件冲突**：唯一约束确保单用户不会重复录入同一证件，如需跨用户合并需调整索引策略。【F:schema.sql†L160-L164】

---

如需进一步了解具体 API 或前端调用流程，可结合 `sky-server` 下 Controller、Service、Mapper 源码阅读。本文档将持续更新，欢迎在代码仓库提交改进建议。
