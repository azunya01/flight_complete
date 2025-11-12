-- 建议：先选库
-- CREATE DATABASE IF NOT EXISTS flight_booking DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
-- USE flight_booking;

create database flight_booking DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

-- 1) user（用户）
CREATE TABLE IF NOT EXISTS `user` (
                                      `id`        VARCHAR(50)  NOT NULL COMMENT '用户ID',
                                      `name`      VARCHAR(50)  NOT NULL COMMENT '用户名',
                                      `password`  VARCHAR(50)  NOT NULL COMMENT '密码',
                                      `type`      INT          NOT NULL COMMENT '1=普通用户,2=管理员',
                                      PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2) airport（机场）
CREATE TABLE IF NOT EXISTS `airport` (
                                         `name`    VARCHAR(50) NOT NULL COMMENT '机场名',
                                         `city`    VARCHAR(50) NOT NULL COMMENT '城市',
                                         `address` VARCHAR(50) NOT NULL COMMENT '地址',
                                         `tel`     VARCHAR(50) NOT NULL COMMENT '电话',
                                         PRIMARY KEY (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3) airline（航线）
CREATE TABLE IF NOT EXISTS `airline` (
                                         `id`            VARCHAR(50) NOT NULL COMMENT '航线编号',
                                         `from_airport`  VARCHAR(50) NOT NULL COMMENT '出发机场',
                                         `to_airport`    VARCHAR(50) NOT NULL COMMENT '到达机场',
                                         PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4) plane（飞机）
CREATE TABLE IF NOT EXISTS `plane` (
                                       `id`    VARCHAR(50) NOT NULL COMMENT '飞机编号',
                                       `type`  VARCHAR(50) NOT NULL COMMENT '飞机类型',
                                       PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5) schedule（时间表）
CREATE TABLE IF NOT EXISTS `schedule` (
                                          `id`         VARCHAR(50) NOT NULL COMMENT '时间表编号',
                                          `from_date`  DATE        NOT NULL COMMENT '起飞日期',
                                          `from_time`  TIME        NOT NULL COMMENT '起飞时间',
                                          `to_date`    DATE        NOT NULL COMMENT '到达日期',
                                          `to_time`    TIME        NOT NULL COMMENT '到达时间',
                                          PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 6) flight（航班）
CREATE TABLE IF NOT EXISTS `flight` (
                                        `id`          VARCHAR(50) NOT NULL COMMENT '航班编号',
                                        `name`        VARCHAR(50) NOT NULL COMMENT '航班名称',
                                        `plane_id`    VARCHAR(50) NOT NULL COMMENT '飞机编号（原设计为外键，这里仅保留字段）',
                                        `airline_id`  VARCHAR(50) NOT NULL COMMENT '航线编号（原设计为外键，这里仅保留字段）',
                                        `schedule_id` VARCHAR(50) NOT NULL COMMENT '时间表编号（原设计为外键，这里仅保留字段）',
                                        PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 7) cabin（客舱）
CREATE TABLE IF NOT EXISTS `cabin` (
                                       `id`              VARCHAR(50) NOT NULL COMMENT '客舱编号',
                                       `name`            VARCHAR(50) NOT NULL COMMENT '客舱名称',
                                       `food`            VARCHAR(50) NOT NULL COMMENT '餐食',
                                       `service`         VARCHAR(50) NOT NULL COMMENT '服务',
                                       `hand_baggage`    VARCHAR(50) NOT NULL COMMENT '手提行李',
                                       `checked_baggage` VARCHAR(50) NOT NULL COMMENT '托运行李',
                                       `price`           DOUBLE      NOT NULL COMMENT '票价',
                                       `capacity`        INT         NOT NULL COMMENT '容量',
                                       `booked`          INT         NOT NULL COMMENT '已预订数量',
                                       `flight_id`       VARCHAR(50) NOT NULL COMMENT '航班编号（原设计为外键，这里仅保留字段）',
                                       PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 8) passenger（乘客）
CREATE TABLE IF NOT EXISTS `passenger` (
                                           `identity` VARCHAR(50) NOT NULL COMMENT '身份证号',
                                           `name`     VARCHAR(50) NOT NULL COMMENT '姓名',
                                           `birthday` DATE        NOT NULL COMMENT '出生日期',
                                           `phone`    VARCHAR(50) NOT NULL COMMENT '联系电话',
                                           PRIMARY KEY (`identity`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 9) orders（订单）
CREATE TABLE IF NOT EXISTS `orders` (
                                        `id`           VARCHAR(50) NOT NULL COMMENT '订单号',
                                        `user_id`      VARCHAR(50) NOT NULL COMMENT '下单用户ID（原设计为外键，这里仅保留字段）',
                                        `passenger_id` VARCHAR(50) NOT NULL COMMENT '乘客身份证号（原设计为外键，这里仅保留字段）',
                                        `flight_id`    VARCHAR(50) NOT NULL COMMENT '航班号（原设计为外键，这里仅保留字段）',
                                        `cabin_id`     VARCHAR(50) NOT NULL COMMENT '客舱号（原设计为外键，这里仅保留字段）',
                                        `status`       INT         NOT NULL COMMENT '1=未支付,2=已支付,3=已取消',
                                        PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 可选：为了查询效率（不加外键也能建索引）
-- CREATE INDEX idx_airline_from ON airline(from_airport);
-- CREATE INDEX idx_airline_to   ON airline(to_airport);
-- CREATE INDEX idx_flight_plane    ON flight(plane_id);
-- CREATE INDEX idx_flight_airline  ON flight(airline_id);
-- CREATE INDEX idx_flight_schedule ON flight(schedule_id);
-- CREATE INDEX idx_cabin_flight    ON cabin(flight_id);
-- CREATE INDEX idx_orders_user     ON orders(user_id);
-- CREATE INDEX idx_orders_passenger ON orders(passenger_id);
-- CREATE INDEX idx_orders_flight   ON orders(flight_id);
-- CREATE INDEX idx_orders_cabin    ON orders(cabin_id);
-- 如果还没选库，先 USE 你的库;
-- USE flight_system;

-- 1) 给 user 表补上 type 列（1=管理员, 2=用户）
ALTER TABLE `user`
    ADD COLUMN `type` TINYINT NOT NULL DEFAULT 2 COMMENT '1=管理员, 2=用户';

-- （可选）确认一下现在的表结构
-- SHOW COLUMNS FROM `user`;
show create table airport;
INSERT INTO `airport` (`name`, `city`, `address`, `tel`)
VALUES
    ('首都国际机场', '北京', '顺义区机场高速路1号', '010-96158'),
    ('虹桥国际机场', '上海', '长宁区虹桥路2550号', '021-96990'),
    ('浦东国际机场', '上海', '浦东新区迎宾大道9000号', '021-96990'),
    ('白云国际机场', '广州', '花都区机场大道南98号', '020-96990'),
    ('宝安国际机场', '深圳', '宝安区机场大道1号', '0755-23456789'),
    ('双流国际机场', '成都', '双流区机场路二段', '028-85205555'),
    ('天府国际机场', '成都', '简阳市芦葭镇', '028-85205555'),
    ('江北国际机场', '重庆', '渝北区机场路168号', '023-67152000'),
    ('萧山国际机场', '杭州', '萧山区靖江镇', '0571-96299'),
    ('禄口国际机场', '南京', '江宁区禄口镇', '025-968890'),
    ('咸阳国际机场', '西安', '咸阳市空港新城', '029-96788'),
    ('白塔国际机场', '沈阳', '浑南区机场路', '024-96890'),
    ('流亭国际机场', '青岛', '城阳区流亭街道', '0532-96567'),
    ('胶东国际机场', '青岛', '胶州市李哥庄镇', '0532-96567'),
    ('滨海国际机场', '天津', '东丽区空港经济区', '022-24906363'),
    ('黄花国际机场', '长沙', '长沙县黄花镇', '0731-96777'),
    ('长水国际机场', '昆明', '官渡区长水镇', '0871-96566'),
    ('龙洞堡国际机场', '贵阳', '南明区龙洞堡', '0851-96967'),
    ('吴圩国际机场', '南宁', '江南区吴圩镇', '0771-2095114'),
    ('高崎国际机场', '厦门', '湖里区高崎镇', '0592-5706018'),
    ('翔安国际机场', '厦门', '翔安区新机场大道', '0592-5706018'),
    ('凤凰国际机场', '三亚', '天涯区凤凰路', '0898-966123'),
    ('美兰国际机场', '海口', '美兰区机场路', '0898-966123'),
    ('新郑国际机场', '郑州', '新郑市航空港区', '0371-96777'),
    ('太原武宿国际机场', '太原', '小店区长风街', '0351-96112'),
    ('合肥新桥国际机场', '合肥', '长丰县岗集镇', '0551-63777888'),
    ('无锡硕放国际机场', '无锡', '新吴区机场路', '0510-962188'),
    ('常州奔牛国际机场', '常州', '新北区奔牛镇', '0519-968897'),
    ('扬州泰州国际机场', '扬州', '邗江区槐泗镇', '0514-96999'),
    ('济南遥墙国际机场', '济南', '历城区遥墙街道', '0531-96877'),
    ('烟台蓬莱国际机场', '烟台', '蓬莱市大辛店镇', '0535-96555'),
    ('威海大水泊国际机场', '威海', '环翠区大水泊镇', '0631-96963'),
    ('南昌昌北国际机场', '南昌', '新建区昌北镇', '0791-96999'),
    ('福州长乐国际机场', '福州', '长乐区漳港镇', '0591-96565'),
    ('泉州晋江国际机场', '泉州', '晋江市灵源街道', '0595-85678901'),
    ('桂林两江国际机场', '桂林', '临桂区两江镇', '0773-2845359'),
    ('珠海金湾机场', '珠海', '金湾区三灶镇', '0756-7771111'),
    ('汕头潮汕国际机场', '揭阳', '揭阳市榕城区', '0663-96556'),
    ('湛江吴川机场', '湛江', '吴川市塘缀镇', '0759-96588'),
    ('兰州中川国际机场', '兰州', '永登县中川镇', '0931-96888'),
    ('银川河东国际机场', '银川', '灵武市东塔镇', '0951-96168'),
    ('乌鲁木齐地窝堡国际机场', '乌鲁木齐', '新市区迎宾路', '0991-96522'),
    ('呼和浩特白塔国际机场', '呼和浩特', '赛罕区白塔村', '0471-96588'),
    ('包头二里半机场', '包头', '昆都仑区机场路', '0472-96577'),
    ('大连周水子国际机场', '大连', '甘井子区虹港路', '0411-96666'),
    ('哈尔滨太平国际机场', '哈尔滨', '道里区太平镇', '0451-96577'),
    ('长春龙嘉国际机场', '长春', '二道区龙嘉镇', '0431-96665'),
    ('延吉朝阳川国际机场', '延吉', '龙井市朝阳川镇', '0433-96555'),
    ('牡丹江海浪国际机场', '牡丹江', '爱民区机场路', '0453-96666'),
    ('齐齐哈尔三家子机场', '齐齐哈尔', '建华区机场路', '0452-96566'),
    ('呼伦贝尔海拉尔机场', '海拉尔', '海拉尔区机场路', '0470-96599'),
    ('拉萨贡嘎国际机场', '拉萨', '贡嘎县甲竹林镇', '0891-95881'),
    ('日喀则和平机场', '日喀则', '定日县机场路', '0892-95959'),
    ('林芝米林机场', '林芝', '米林县机场路', '0894-95882'),
    ('香港国际机场', '香港', '赤鱲角香港国际机场', '852-21818888'),
    ('澳门国际机场', '澳门', '氹仔机场道', '853-28861111'),
    ('台北桃园国际机场', '台北', '桃园市大园区航站南路9号', '02-412-8008'),
    ('高雄小港国际机场', '高雄', '小港区中山四路', '07-8069700'),
    ('东京羽田国际机场', '东京', '大田区羽田空港', '+81-3-5757-8111'),
    ('东京成田国际机场', '东京', '成田市古込1号', '+81-476-34-8000'),
    ('首尔仁川国际机场', '首尔', '仁川市中区机场路272号', '+82-32-741-0114'),
    ('釜山金海国际机场', '釜山', '江西区空港路108号', '+82-51-974-3775'),
    ('曼谷素万那普国际机场', '曼谷', '拉差贴瓦区', '+66-2-132-1888'),
    ('新加坡樟宜机场', '新加坡', '樟宜东路', '+65-6595-6868'),
    ('吉隆坡国际机场', '吉隆坡', '雪邦区机场大道', '+60-3-8776-2000'),
    ('河内内排国际机场', '河内', '内排镇机场路', '+84-24-3886-5047'),
    ('胡志明新山一国际机场', '胡志明市', '新平郡机场路', '+84-28-3848-5383'),
    ('雅加达苏加诺-哈达国际机场', '雅加达', '班登省机场路', '+62-21-550-5179'),
    ('新德里英迪拉·甘地国际机场', '新德里', '巴拉德希尔机场路', '+91-124-3376000'),
    ('伦敦希思罗机场', '伦敦', 'Longford TW6, UK', '+44-844-335-1801'),
    ('伦敦盖特威克机场', '伦敦', 'Horley, UK', '+44-844-892-0322'),
    ('巴黎戴高乐机场', '巴黎', 'Roissy-en-France', '+33-1-7036-3950'),
    ('巴黎奥利机场', '巴黎', 'Orly, France', '+33-1-4975-1515'),
    ('法兰克福国际机场', '法兰克福', 'Frankfurt am Main', '+49-69-6900'),
    ('慕尼黑国际机场', '慕尼黑', 'Nordallee 25', '+49-89-97500'),
    ('阿姆斯特丹史基浦机场', '阿姆斯特丹', 'Evert van de Beekstraat 202', '+31-20-794-0800'),
    ('苏黎世国际机场', '苏黎世', 'Flughafenstrasse', '+41-43-816-2211'),
    ('马德里巴拉哈斯机场', '马德里', 'Av de la Hispanidad', '+34-913-211-000'),
    ('罗马菲乌米奇诺机场', '罗马', 'Via dell\'Aeroporto', '+39-06-65951'),
    ('米兰马尔彭萨机场', '米兰', 'Ferno VA', '+39-02-232323'),
    ('布鲁塞尔国际机场', '布鲁塞尔', 'Leopoldlaan', '+32-2-753-7753'),
    ('维也纳国际机场', '维也纳', 'Wien-Flughafen', '+43-1-70070'),
    ('哥本哈根国际机场', '哥本哈根', 'Lufthavnsboulevarden', '+45-3231-3231'),
    ('奥斯陆加勒穆恩机场', '奥斯陆', 'Edvard Munchs veg', '+47-6481-2000'),
    ('斯德哥尔摩阿兰达机场', '斯德哥尔摩', 'Arlanda Sigtuna', '+46-10-109-1000'),
    ('纽约肯尼迪国际机场', '纽约', 'Queens NY 11430', '+1-718-244-4444'),
    ('纽约拉瓜迪亚机场', '纽约', 'Queens NY 11371', '+1-718-533-3400'),
    ('洛杉矶国际机场', '洛杉矶', '1 World Way', '+1-855-463-5252'),
    ('旧金山国际机场', '旧金山', 'San Francisco, CA 94128', '+1-650-821-8211'),
    ('芝加哥奥黑尔国际机场', '芝加哥', '10000 W O\'Hare Ave', '+1-800-832-6352'),
    ('达拉斯沃斯堡国际机场', '达拉斯', 'DFW Airport, TX', '+1-972-973-3112'),
    ('迈阿密国际机场', '迈阿密', '2100 NW 42nd Ave', '+1-305-876-7000'),
    ('多伦多皮尔逊国际机场', '多伦多', '6301 Silver Dart Dr', '+1-416-247-7678'),
    ('温哥华国际机场', '温哥华', '3211 Grant McConachie Way', '+1-604-207-7077'),
    ('墨尔本国际机场', '墨尔本', 'Tullamarine VIC 3045', '+61-3-9297-1600'),
    ('悉尼金斯福德·史密斯机场', '悉尼', 'Mascot NSW 2020', '+61-2-9667-9111'),
    ('奥克兰国际机场', '奥克兰', 'Ray Emery Dr, Māngere', '+64-9-275-0789'),
    ('惠灵顿国际机场', '惠灵顿', 'Stewart Duff Dr', '+64-4-385-5100');



show create table plane;
INSERT INTO `plane` (`name`)
VALUES
    ('Airbus A320'),
    ('Airbus A321'),
    ('Airbus A319'),
    ('Airbus A330'),
    ('Airbus A350'),
    ('Airbus A380'),
    ('Boeing 737-700'),
    ('Boeing 737-800'),
    ('Boeing 737 MAX 8'),
    ('Boeing 737 MAX 9'),
    ('Boeing 747-400'),
    ('Boeing 747-8'),
    ('Boeing 757-200'),
    ('Boeing 767-300'),
    ('Boeing 777-200'),
    ('Boeing 777-300ER'),
    ('Boeing 787-8'),
    ('Boeing 787-9'),
    ('Boeing 787-10'),
    ('COMAC C919'),
    ('COMAC ARJ21'),
    ('Embraer E190'),
    ('Embraer E195'),
    ('Bombardier CRJ900'),
    ('Bombardier Q400'),
    ('Airbus A320neo'),
    ('Airbus A321neo'),
    ('Airbus A330neo'),
    ('Airbus A340'),
    ('Airbus A300'),
    ('Boeing 727'),
    ('Boeing 717'),
    ('Boeing 707'),
    ('Boeing 720'),
    ('Boeing 737-200'),
    ('Boeing 737-300'),
    ('Boeing 737-400'),
    ('Boeing 737-500'),
    ('Boeing 757-300'),
    ('Boeing 767-400ER'),
    ('Boeing 777X'),
    ('Boeing 787 Dreamliner'),
    ('Antonov An-124'),
    ('Antonov An-225'),
    ('Tupolev Tu-204'),
    ('Tupolev Tu-154'),
    ('Ilyushin Il-96'),
    ('Sukhoi Superjet 100'),
    ('McDonnell Douglas MD-80'),
    ('McDonnell Douglas MD-90'),
    ('McDonnell Douglas DC-9'),
    ('McDonnell Douglas DC-10'),
    ('Lockheed L-1011 Tristar'),
    ('ATR 72'),
    ('ATR 42'),
    ('De Havilland Dash 8'),
    ('Fairchild Dornier 328JET'),
    ('Saab 340'),
    ('Saab 2000'),
    ('Fokker 100'),
    ('Fokker 70'),
    ('Fokker 50'),
    ('Fokker 27'),
    ('Cessna Citation X'),
    ('Cessna 560XL'),
    ('Cessna 525CJ'),
    ('Gulfstream G550'),
    ('Gulfstream G650'),
    ('Dassault Falcon 7X'),
    ('Dassault Falcon 900'),
    ('Embraer Legacy 600'),
    ('Embraer Phenom 300'),
    ('Pilatus PC-12'),
    ('Beechcraft King Air 350'),
    ('Learjet 60'),
    ('Learjet 45'),
    ('Learjet 75'),
    ('HondaJet HA-420'),
    ('Airbus Beluga'),
    ('Airbus BelugaXL'),
    ('Boeing 737 Cargo'),
    ('Boeing 747 Cargo'),
    ('Boeing 767 Cargo'),
    ('Boeing 777 Cargo'),
    ('Airbus A330 Cargo'),
    ('Airbus A350F Cargo'),
    ('COMAC CR929'),
    ('Xian MA60'),
    ('Xian MA700'),
    ('Harbin Y-12'),
    ('Harbin Y-7'),
    ('Shenyang J-11'),
    ('Chengdu J-10'),
    ('Shaanxi Y-8'),
    ('Yakovlev Yak-42'),
    ('Yakovlev Yak-40'),
    ('Sukhoi Su-27'),
    ('Sukhoi Su-30'),
    ('Sukhoi Su-35'),
    ('Lockheed C-130'),
    ('Lockheed C-5 Galaxy');
show create table plane;
show create table flight;

-- 1) 舱位类型（小字典表）
CREATE TABLE `seat_type` (
                             `id` TINYINT NOT NULL COMMENT '舱位ID：1=经济,2=商务,3=头等...',
                             `name` VARCHAR(20) NOT NULL COMMENT '舱位名称',
                             `code` VARCHAR(10) NOT NULL COMMENT '舱位码：Y/C/F 等',
                             PRIMARY KEY (`id`),
                             UNIQUE KEY `uk_seat_type_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- 2) 飞机（保留你已有结构，补充索引；无外键）


-- 3) 航班（保留你已有字段，增加合理索引；无外键）
CREATE TABLE `flight` (
                          `id` INT NOT NULL AUTO_INCREMENT COMMENT '航班编号',
                          `name` VARCHAR(50) NOT NULL COMMENT '航班名称',
                          `plane_id` INT NOT NULL COMMENT '飞机编号（仅字段，不建外键）',
                          `airline_id` INT NOT NULL COMMENT '航线编号（仅字段，不建外键）',
                          `departure_time` DATETIME NOT NULL COMMENT '起飞时间',
                          `arrival_time` DATETIME NOT NULL COMMENT '降落时间',
                          `price` DECIMAL(10,2) NOT NULL COMMENT '基础价格/参考价',
                          PRIMARY KEY (`id`),
                          KEY `idx_flight_plane` (`plane_id`),
                          KEY `idx_flight_airline` (`airline_id`),
                          KEY `idx_flight_departure_time` (`departure_time`),
                          KEY `idx_flight_name` (`name`),
                          CHECK (`arrival_time` > `departure_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- 4) 飞机→舱位“容量模板”（定义每架/型飞机的各舱位最大可售数）
--    无外键，用复合主键保证唯一：一架飞机的一个舱位仅一行
CREATE TABLE `plane_seat_capacity` (
                                       `plane_id` INT NOT NULL COMMENT '对应的飞机',
                                       `seat_type_id` TINYINT NOT NULL COMMENT '舱位ID（参考 seat_type.id）',
                                       `capacity` INT NOT NULL COMMENT '该舱位最大可售座位数',
                                       PRIMARY KEY (`plane_id`, `seat_type_id`),
                                       KEY `idx_psc_seat_type` (`seat_type_id`),
                                       CHECK (`capacity` >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- 5) 航班→舱位“可售库存”（创建航班时由模板拷贝一份到这里）
--    无外键，复合主键保证“一个航班的一个舱位仅一行”
CREATE TABLE `flight_seat_inventory` (
                                         `flight_id` INT NOT NULL COMMENT '航班',
                                         `seat_type_id` TINYINT NOT NULL COMMENT '舱位ID',
                                         `total` INT NOT NULL COMMENT '该航班该舱位总量（通常=模板容量）',
                                         `available` INT NOT NULL COMMENT '当前可售余量',
                                         `price` DECIMAL(10,2) NOT NULL COMMENT '该航班该舱位价格（可与flight.price一致或按规则生成）',
                                         PRIMARY KEY (`flight_id`, `seat_type_id`),
                                         KEY `idx_fsi_seat_type` (`seat_type_id`),
                                         CHECK (`total` >= 0),
                                         CHECK (`available` >= 0),
                                         CHECK (`available` <= `total`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
show create table flight;
show create table flight_seat_inventory;

show create table plane_seat_capacity;
drop table schedule;
SELECT
             f.id                       AS id,
             f.name                     AS name,
             p.name                     AS planeName,
             al.from_airport        AS departureAirportName,
             al.to_airport               AS arrivalAirportName,
             f.departure_time           AS departureTime,
             f.arrival_time             AS arrivalTime
         FROM flight f
                  LEFT JOIN plane   p        ON p.id = f.plane_id
                  LEFT JOIN airline al       ON al.id = f.airline_id
         ORDER BY f.departure_time , f.id
            LIMIT 10 OFFSET 0;

CREATE TABLE plane_seat (
                            plane_id      INT         NOT NULL COMMENT '飞机ID（仅字段，不建外键）',
                            seat_no       VARCHAR(5)  NOT NULL COMMENT '座位号，如 12A',
                            seat_type_id  TINYINT     NOT NULL COMMENT '舱位ID：1=Y/经济, 2=C/商务, 3=F/头等…',
                            `row_number`    SMALLINT    NOT NULL COMMENT '行号：便于排序/渲染',
                            seat_col      CHAR(1)     NOT NULL COMMENT '列字母：A/B/C/…',
                            PRIMARY KEY (plane_id, seat_no),
                            KEY idx_ps_type (plane_id, seat_type_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模板：定义某架飞机的座位布局';
CREATE TABLE flight_seat (
                             flight_id     INT         NOT NULL COMMENT '航班ID（仅字段，不建外键）',
                             seat_no       VARCHAR(5)  NOT NULL COMMENT '座位号：每个航班都有自己的 12A',
                             seat_type_id  TINYINT     NOT NULL COMMENT '舱位ID',
                             status        TINYINT     NOT NULL DEFAULT 0 COMMENT '0可售 1锁定 2已售 3屏蔽',
                             price         DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '本座位售卖价（默认用舱位价）',
                             PRIMARY KEY (flight_id, seat_no),
                             KEY idx_fs_status (flight_id, status),
                             KEY idx_fs_type   (flight_id, seat_type_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='实例：本次航班的可选座';

show create table plane_seat_capacity;
show create table seat_type;

-- =========================================================
-- 1) 存储过程：按单一 舱位类型 重新生成某飞机的座位模板
--    规则：seat_no = seat_type.code || '-' || LPAD(seq, width, '0')
--    width = IF(capacity >= 100, 3, 2)
--    行/列：row_no = FLOOR((n-1)/per_row)+1, col_no=((n-1)%per_row)+1
-- =========================================================
DELIMITER $$

DROP PROCEDURE IF EXISTS regen_plane_seats_for_type $$
CREATE PROCEDURE regen_plane_seats_for_type(IN p_plane_id INT, IN p_seat_type_id INT)
BEGIN
    DECLARE v_cap INT;
    DECLARE v_per_row INT;
    DECLARE v_code VARCHAR(10);
    DECLARE v_width INT;

    -- 取容量、每行人数、舱位码
    SELECT psc.capacity,
           COALESCE(psc.row_number, 1),
           st.code
    INTO v_cap, v_per_row, v_code
    FROM plane_seat_capacity psc
             JOIN seat_type st ON st.id = psc.seat_type_id
    WHERE psc.plane_id = p_plane_id AND psc.seat_type_id = p_seat_type_id
    LIMIT 1;

    -- 先清空该飞机该舱位的旧模板
    DELETE FROM plane_seat
    WHERE plane_id = p_plane_id AND seat_type_id = p_seat_type_id;

    -- 若未配置或容量<=0，直接返回
    IF v_cap IS NULL OR v_cap <= 0 THEN
        LEAVE proc_body;
    END IF;

    -- 宽度：两位或三位
    SET v_width = IF(v_cap >= 100, 3, 2);

    -- 递归序列 1..v_cap 生成逐个座位
    WITH RECURSIVE seq(n) AS (
        SELECT 1
        UNION ALL
        SELECT n + 1 FROM seq WHERE n < v_cap
    )
    INSERT INTO plane_seat (plane_id, seat_no, seat_type_id, row_no, col_no)
    SELECT
        p_plane_id                                                  AS plane_id,
        CONCAT(v_code, '-', LPAD(n, v_width, '0'))                  AS seat_no,
        p_seat_type_id                                              AS seat_type_id,
        FLOOR((n - 1) / GREATEST(v_per_row,1)) + 1                  AS row_no,
        ((n - 1) % GREATEST(v_per_row,1)) + 1                       AS col_no
    FROM seq;

    proc_body: BEGIN END;
END $$

-- =========================================================
-- 2) 存储过程：按“整架飞机”重建全部舱位的模板
-- =========================================================
DROP PROCEDURE IF EXISTS regen_plane_seats_all $$
CREATE PROCEDURE regen_plane_seats_all(IN p_plane_id INT)
BEGIN
    -- 清空整架飞机的模板（避免残留）
    DELETE FROM plane_seat WHERE plane_id = p_plane_id;

    -- 对该飞机配置的所有舱位逐个生成
    -- 注意：用游标逐条调用上面的过程，确保不同舱位独立计算
    DECLARE done INT DEFAULT 0;
  DECLARE v_seat_type_id INT;
  DECLARE cur CURSOR FOR
    SELECT seat_type_id FROM plane_seat_capacity WHERE plane_id = p_plane_id;
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;

    OPEN cur;
    read_loop: LOOP
        FETCH cur INTO v_seat_type_id;
        IF done = 1 THEN
            LEAVE read_loop;
        END IF;

        CALL regen_plane_seats_for_type(p_plane_id, v_seat_type_id);
    END LOOP;
    CLOSE cur;
END $$

DELIMITER ;

-- =========================================================
-- 3) 触发器：对 plane_seat_capacity 的变更自动重建模板
--    - INSERT/UPDATE：重建该 舱位 类型 的模板
--    - DELETE：删除该 舱位 类型 的模板
-- =========================================================
DROP TRIGGER IF EXISTS trg_psc_ai;
DROP TRIGGER IF EXISTS trg_psc_au;
DROP TRIGGER IF EXISTS trg_psc_ad;

DELIMITER $$

CREATE TRIGGER trg_psc_ai
    AFTER INSERT ON plane_seat_capacity
    FOR EACH ROW
BEGIN
    CALL regen_plane_seats_for_type(NEW.plane_id, NEW.seat_type_id);
END $$

CREATE TRIGGER trg_psc_au
    AFTER UPDATE ON plane_seat_capacity
    FOR EACH ROW
BEGIN
    -- 如果 plane_id 或 seat_type_id 被改了，需要两边都处理；最简单是全量重建
    IF NEW.plane_id <> OLD.plane_id OR NEW.seat_type_id <> OLD.seat_type_id THEN
        -- 清理老的
        DELETE FROM plane_seat WHERE plane_id = OLD.plane_id AND seat_type_id = OLD.seat_type_id;
        -- 新的重建
        CALL regen_plane_seats_for_type(NEW.plane_id, NEW.seat_type_id);
    ELSE
        -- 容量或每行人数变化：重建该类型即可
        CALL regen_plane_seats_for_type(NEW.plane_id, NEW.seat_type_id);
    END IF;
END $$

CREATE TRIGGER trg_psc_ad
    AFTER DELETE ON plane_seat_capacity
    FOR EACH ROW
BEGIN
    DELETE FROM plane_seat WHERE plane_id = OLD.plane_id AND seat_type_id = OLD.seat_type_id;
END $$

DELIMITER ;


DELIMITER $$



DROP PROCEDURE IF EXISTS regen_plane_seats_for_type $$
CREATE PROCEDURE regen_plane_seats_for_type(IN p_plane_id INT, IN p_seat_type_id INT)
BEGIN
    DECLARE v_cap INT DEFAULT 0;
    DECLARE v_per_row INT DEFAULT 1;
    DECLARE v_code VARCHAR(10);
    DECLARE v_width INT;

    -- 读取容量、每行人数与舱位码
    SELECT psc.capacity,
           COALESCE(psc.row_number, 1),
           st.code
    INTO v_cap, v_per_row, v_code
    FROM plane_seat_capacity psc
             JOIN seat_type st ON st.id = psc.seat_type_id
    WHERE psc.plane_id = p_plane_id AND psc.seat_type_id = p_seat_type_id
    LIMIT 1;

    -- 先清空该飞机该舱位的旧模板
    DELETE FROM plane_seat
    WHERE plane_id = p_plane_id AND seat_type_id = p_seat_type_id;

    -- 若未配置或容量<=0，直接结束（无需 LEAVE）
    IF v_cap IS NULL OR v_cap <= 0 OR v_code IS NULL THEN
        -- 不生成任何座位
    ELSE
        SET v_width = IF(v_cap >= 100, 3, 2);

        WITH RECURSIVE seq(n) AS (
            SELECT 1
            UNION ALL
            SELECT n + 1 FROM seq WHERE n < v_cap
        )
        INSERT INTO plane_seat (plane_id, seat_no, seat_type_id, `row_no`, seat_col)
        SELECT
            p_plane_id                                                   AS plane_id,
            CONCAT(v_code, '-', LPAD(n, v_width, '0'))                   AS seat_no,
            p_seat_type_id                                               AS seat_type_id,
            FLOOR((n - 1) / GREATEST(v_per_row,1)) + 1                   AS row_no,
            ((n - 1) % GREATEST(v_per_row,1)) + 1                        AS col_no
        FROM seq;
    END IF;
END $$

DELIMITER ;




DELIMITER $$

DROP PROCEDURE IF EXISTS regen_plane_seats_for_type $$
CREATE PROCEDURE regen_plane_seats_for_type(IN p_plane_id INT, IN p_seat_type_id INT)
proc: BEGIN
    DECLARE v_cap INT DEFAULT 0;
    DECLARE v_per_row INT DEFAULT 1;
    DECLARE v_code VARCHAR(10);
    DECLARE v_width INT DEFAULT 2;
    DECLARE i INT DEFAULT 1;
    DECLARE v_row INT;
    DECLARE v_col INT;
    DECLARE v_seq VARCHAR(8);
    DECLARE v_seat_no VARCHAR(32);

    -- 读取容量、每行人数与舱位码
    SELECT psc.capacity,
           COALESCE(psc.row_number, 1),
           st.code
    INTO v_cap, v_per_row, v_code
    FROM plane_seat_capacity psc
             JOIN seat_type st ON st.id = psc.seat_type_id
    WHERE psc.plane_id = p_plane_id AND psc.seat_type_id = p_seat_type_id
    LIMIT 1;

    -- 清空该飞机该舱位旧模板
    DELETE FROM plane_seat
    WHERE plane_id = p_plane_id AND seat_type_id = p_seat_type_id;

    -- 判空/非法直接退出
    IF v_cap IS NULL OR v_cap <= 0 OR v_code IS NULL THEN
        LEAVE proc;
    END IF;

    -- 序号宽度
    SET v_width = IF(v_cap >= 100, 3, 2);
    SET v_per_row = GREATEST(v_per_row, 1);

    -- 逐个生成 1..v_cap
    WHILE i <= v_cap DO
            SET v_row = FLOOR((i - 1) / v_per_row) + 1;
            SET v_col = ((i - 1) % v_per_row) + 1;
            SET v_seq = LPAD(i, v_width, '0');
            SET v_seat_no = CONCAT(v_code, '-', v_seq);

            INSERT INTO plane_seat (plane_id, seat_no, seat_type_id, row_no, seat_col)
            VALUES (p_plane_id, v_seat_no, p_seat_type_id, v_row, v_col);

            SET i = i + 1;
        END WHILE;
END $$
DELIMITER ;



DELIMITER $$

DROP PROCEDURE IF EXISTS regen_plane_seats_all $$
CREATE PROCEDURE regen_plane_seats_all(IN p_plane_id INT)
BEGIN
    -- ✅ 所有 DECLARE 必须在块最前面
    DECLARE done INT DEFAULT 0;
    DECLARE v_seat_type_id INT;
    DECLARE cur CURSOR FOR
        SELECT seat_type_id FROM plane_seat_capacity WHERE plane_id = p_plane_id;
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;

    -- 先清空整架飞机模板（防残留）
    DELETE FROM plane_seat WHERE plane_id = p_plane_id;

    OPEN cur;
    read_loop: LOOP
        FETCH cur INTO v_seat_type_id;
        IF done = 1 THEN
            LEAVE read_loop;
        END IF;

        CALL regen_plane_seats_for_type(p_plane_id, v_seat_type_id);
    END LOOP;
    CLOSE cur;
END $$
DELIMITER ;


DROP TRIGGER IF EXISTS trg_psc_ai;
DROP TRIGGER IF EXISTS trg_psc_au;
DROP TRIGGER IF EXISTS trg_psc_ad;

DELIMITER $$

CREATE TRIGGER trg_psc_ai
    AFTER INSERT ON plane_seat_capacity
    FOR EACH ROW
BEGIN
    CALL regen_plane_seats_for_type(NEW.plane_id, NEW.seat_type_id);
END $$

CREATE TRIGGER trg_psc_au
    AFTER UPDATE ON plane_seat_capacity
    FOR EACH ROW
BEGIN
    IF NEW.plane_id <> OLD.plane_id OR NEW.seat_type_id <> OLD.seat_type_id THEN
        DELETE FROM plane_seat WHERE plane_id = OLD.plane_id AND seat_type_id = OLD.seat_type_id;
    END IF;
    CALL regen_plane_seats_for_type(NEW.plane_id, NEW.seat_type_id);
END $$

CREATE TRIGGER trg_psc_ad
    AFTER DELETE ON plane_seat_capacity
    FOR EACH ROW
BEGIN
    DELETE FROM plane_seat WHERE plane_id = OLD.plane_id AND seat_type_id = OLD.seat_type_id;
END $$

DELIMITER ;


show create table plane_seat_capacity;
show create table seat_type;
show create table plane_seat;

DELIMITER $$

DROP PROCEDURE IF EXISTS regen_plane_seats_for_type $$
CREATE PROCEDURE regen_plane_seats_for_type(IN p_plane_id INT, IN p_seat_type_id INT)
proc: BEGIN
    DECLARE v_cap INT DEFAULT 0;           -- 容量（总座位数）
    DECLARE v_per_row INT DEFAULT 1;       -- 每行人数（来自 row_number）
    DECLARE v_code VARCHAR(10);            -- 舱位码（Y/C/F 等）
    DECLARE v_width INT DEFAULT 2;         -- 序号宽度（<100 两位，否则三位）
    DECLARE i INT DEFAULT 1;               -- 计数器 1..v_cap
    DECLARE v_row INT;                     -- 行号
    DECLARE v_col_idx INT;                 -- 列序号(1..v_per_row)
    DECLARE v_seat_col CHAR(1);            -- 列字母 A/B/C/...
    DECLARE v_seq VARCHAR(8);              -- 序号字符串 01/002...
    DECLARE v_seat_no VARCHAR(16);         -- 最终座位号 code-xx

    -- 读取容量、每行人数与舱位码
    SELECT psc.capacity,
           COALESCE(psc.row_number, 1),
           st.code
    INTO v_cap, v_per_row, v_code
    FROM plane_seat_capacity psc
             JOIN seat_type st ON st.id = psc.seat_type_id
    WHERE psc.plane_id = p_plane_id AND psc.seat_type_id = p_seat_type_id
    LIMIT 1;

    -- 清空该飞机该舱位的旧模板
    DELETE FROM plane_seat
    WHERE plane_id = p_plane_id AND seat_type_id = p_seat_type_id;

    -- 判空/非法直接退出
    IF v_cap IS NULL OR v_cap <= 0 OR v_code IS NULL THEN
        LEAVE proc;
    END IF;

    -- 宽度与保护
    SET v_width   = IF(v_cap >= 100, 3, 2);
    SET v_per_row = GREATEST(v_per_row, 1);

    -- 逐个生成 1..v_cap
    WHILE i <= v_cap DO
            SET v_row      = FLOOR((i - 1) / v_per_row) + 1;
            SET v_col_idx  = ((i - 1) % v_per_row) + 1;
            SET v_seat_col = CHAR(64 + v_col_idx);       -- 1->A, 2->B, ...

            SET v_seq     = LPAD(i, v_width, '0');       -- 01/02/.../120
            SET v_seat_no = CONCAT(v_code, '-', v_seq);  -- Y-01 / C-12 / F-120

            INSERT INTO plane_seat (plane_id, seat_no, seat_type_id, row_no, seat_col)
            VALUES (p_plane_id, v_seat_no, p_seat_type_id, v_row, v_seat_col);

            SET i = i + 1;
        END WHILE;
END $$
DELIMITER ;


DELIMITER $$

DROP PROCEDURE IF EXISTS regen_plane_seats_all $$
CREATE PROCEDURE regen_plane_seats_all(IN p_plane_id INT)
BEGIN
    -- 声明必须在最前
    DECLARE done INT DEFAULT 0;
    DECLARE v_seat_type_id INT;
    DECLARE cur CURSOR FOR
        SELECT seat_type_id FROM plane_seat_capacity WHERE plane_id = p_plane_id;
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;

    -- 先清空整架飞机模板（防残留）
    DELETE FROM plane_seat WHERE plane_id = p_plane_id;

    OPEN cur;
    read_loop: LOOP
        FETCH cur INTO v_seat_type_id;
        IF done = 1 THEN
            LEAVE read_loop;
        END IF;
        CALL regen_plane_seats_for_type(p_plane_id, v_seat_type_id);
    END LOOP;
    CLOSE cur;
END $$
DELIMITER ;



DROP TRIGGER IF EXISTS trg_psc_ai;
DROP TRIGGER IF EXISTS trg_psc_au;
DROP TRIGGER IF EXISTS trg_psc_ad;

DELIMITER $$

-- 插入：生成该舱位的座位
CREATE TRIGGER trg_psc_ai
    AFTER INSERT ON plane_seat_capacity
    FOR EACH ROW
BEGIN
    CALL regen_plane_seats_for_type(NEW.plane_id, NEW.seat_type_id);
END $$

-- 更新：若更换了 plane_id/seat_type_id 先清老数据；其余直接重建该舱位
CREATE TRIGGER trg_psc_au
    AFTER UPDATE ON plane_seat_capacity
    FOR EACH ROW
BEGIN
    IF NEW.plane_id <> OLD.plane_id OR NEW.seat_type_id <> OLD.seat_type_id THEN
        DELETE FROM plane_seat WHERE plane_id = OLD.plane_id AND seat_type_id = OLD.seat_type_id;
    END IF;
    CALL regen_plane_seats_for_type(NEW.plane_id, NEW.seat_type_id);
END $$

-- 删除：清理对应模板
CREATE TRIGGER trg_psc_ad
    AFTER DELETE ON plane_seat_capacity
    FOR EACH ROW
BEGIN
    DELETE FROM plane_seat WHERE plane_id = OLD.plane_id AND seat_type_id = OLD.seat_type_id;
END $$

DELIMITER ;




DELIMITER $$

DROP PROCEDURE IF EXISTS regen_plane_seats_for_type $$
CREATE PROCEDURE regen_plane_seats_for_type(IN p_plane_id INT, IN p_seat_type_id INT)
proc: BEGIN
    DECLARE v_cap INT DEFAULT 0;           -- 总座位数
    DECLARE v_per_row INT DEFAULT 1;       -- 每行人数（row_number）
    DECLARE v_code VARCHAR(10);            -- 舱位码（J/Y/F/C 等）
    DECLARE v_width INT DEFAULT 2;         -- 行号位数(<100 两位，否则三位)
    DECLARE i INT DEFAULT 1;               -- 计数器 1..v_cap
    DECLARE v_row INT;                     -- 行号（1..）
    DECLARE v_col_idx INT;                 -- 列序号（1..v_per_row）
    DECLARE v_seat_col CHAR(1);            -- 列字母（A/B/C/...）
    DECLARE v_row_str VARCHAR(8);          -- 行号字符串 01/002/...
    DECLARE v_seat_no VARCHAR(16);         -- 最终座位号 J-A01 等

    -- 读取配置
    SELECT psc.capacity,
           COALESCE(psc.row_number, 1),
           st.code
    INTO v_cap, v_per_row, v_code
    FROM plane_seat_capacity psc
             JOIN seat_type st ON st.id = psc.seat_type_id
    WHERE psc.plane_id = p_plane_id AND psc.seat_type_id = p_seat_type_id
    LIMIT 1;

    -- 清空该舱位旧模板
    DELETE FROM plane_seat
    WHERE plane_id = p_plane_id AND seat_type_id = p_seat_type_id;

    -- 无配置或非法直接退出
    IF v_cap IS NULL OR v_cap <= 0 OR v_code IS NULL THEN
        LEAVE proc;
    END IF;

    -- 保护与宽度
    SET v_per_row = GREATEST(v_per_row, 1);
    SET v_width   = IF(v_cap >= 100, 3, 2);

    -- 生成 1..v_cap
    WHILE i <= v_cap DO
            SET v_row      = FLOOR((i - 1) / v_per_row) + 1;   -- 第几行
            SET v_col_idx  = ((i - 1) % v_per_row) + 1;        -- 该行第几列
            SET v_seat_col = CHAR(64 + v_col_idx);             -- 1->A, 2->B, ...

            SET v_row_str  = LPAD(v_row, v_width, '0');        -- 01 / 12 / 120
            SET v_seat_no  = CONCAT(v_code, '-', v_seat_col, v_row_str);  -- J-A01 / Y-F12

            INSERT INTO plane_seat (plane_id, seat_no, seat_type_id, row_no, seat_col)
            VALUES (p_plane_id, v_seat_no, p_seat_type_id, v_row, v_seat_col);

            SET i = i + 1;
        END WHILE;
END $$
DELIMITER ;

show create table airport;
show create table airline;
show create table flight;
show create table flight_seat_inventory;
drop table passenger;
drop table orders;
-- 订单头：一单可含多张票
DROP TABLE IF EXISTS booking_order;
CREATE TABLE booking_order (
                               id               BIGINT        NOT NULL AUTO_INCREMENT COMMENT '订单ID',
                               order_no         VARCHAR(32)   NOT NULL COMMENT '业务单号（唯一）',
                               user_id          VARCHAR(64)            COMMENT '下单用户（可空）',
                               flight_id        INT           NOT NULL COMMENT '航班ID（冗余便于检索）',
                               status           TINYINT       NOT NULL DEFAULT 1 COMMENT '1已支付(简化) 2已取消 3已退款',
                               total_amount     DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '订单总金额',
                               currency         CHAR(3)       NOT NULL DEFAULT 'CNY',
                               idempotency_key  CHAR(36)               COMMENT '幂等键（可选，防重复提交）',
                               created_at       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
                               PRIMARY KEY (id),
                               UNIQUE KEY uk_order_no (order_no),
                               UNIQUE KEY uk_idem    (idempotency_key),
                               KEY idx_order_flight (flight_id),
                               KEY idx_order_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='最小订单头';

-- 订单明细：每位乘客一行（不考虑座位号）
DROP TABLE IF EXISTS booking_order_item;
CREATE TABLE booking_order_item (
                                    id               BIGINT        NOT NULL AUTO_INCREMENT COMMENT '明细ID',
                                    order_id         BIGINT        NOT NULL COMMENT '订单ID（仅字段）',
                                    flight_id        INT           NOT NULL COMMENT '航班ID（冗余）',
                                    seat_type_id     TINYINT       NOT NULL COMMENT '舱位ID',
                                    price            DECIMAL(10,2) NOT NULL COMMENT '成交价（按下单时锁定）',
                                    passenger_name   VARCHAR(50)   NOT NULL COMMENT '乘客姓名',
                                    passenger_id_no  VARCHAR(32)   NOT NULL COMMENT '证件号',
                                    created_at       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                    PRIMARY KEY (id),
                                    KEY idx_item_order  (order_id),
                                    KEY idx_item_flight (flight_id),
                                    KEY idx_item_type   (flight_id, seat_type_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='最小订单明细';
DROP TABLE IF EXISTS passenger;
CREATE TABLE passenger (
                           id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '乘客ID',
                           user_id     VARCHAR(64)           COMMENT '所属用户（未登录可空）',
                           name        VARCHAR(50)  NOT NULL COMMENT '姓名',
                           gender      CHAR(1)               COMMENT 'M/F（可空）',
                           id_type     TINYINT      NOT NULL DEFAULT 1 COMMENT '证件类型：1身份证 2护照 3台胞证 4回乡证…',
                           id_no       VARCHAR(32)  NOT NULL COMMENT '证件号',
                           phone       VARCHAR(20)           COMMENT '手机号（可空）',
                           birthday    DATE                  COMMENT '生日（可空）',
                           created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           updated_at  DATETIME              COMMENT '更新时间',
                           PRIMARY KEY (id),
    -- 同一用户下，证件类型+证件号唯一，避免重复录入
                           UNIQUE KEY uk_passenger_user_doc (user_id, id_type, id_no),
                           KEY idx_passenger_user (user_id),
                           KEY idx_passenger_idno (id_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='常用乘客主表（下单可复用）';


show create table passenger;
DROP TABLE IF EXISTS passenger;
CREATE TABLE passenger (
                           id          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '乘客ID',
                           user_id     VARCHAR(64)            COMMENT '所属用户ID/账号（可与 booking_order.user_id 对齐）',
                           name        VARCHAR(50)   NOT NULL COMMENT '姓名',
                           gender      CHAR(1)                COMMENT 'M/F（可空）',
                           id_no       VARCHAR(32)   NOT NULL COMMENT '证件号',
                           phone       VARCHAR(20)            COMMENT '手机号（可空）',
                           created_at  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           updated_at  DATETIME               COMMENT '更新时间',
                           PRIMARY KEY (id),
                           UNIQUE KEY uk_passenger_user_doc (user_id, id_no),    -- 关键唯一约束
                           KEY idx_passenger_user   (user_id),
                           KEY idx_passenger_idno   (id_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='常用乘客主表（按用户隔离）';
SELECT f.id AS id, f.name AS name, p.name AS planeName, al.from_airport AS departureAirportName, al.to_airport AS arrivalAirportName, f.departure_time AS departureTime, f.arrival_time AS arrivalTime FROM flight f LEFT JOIN plane p ON p.id = f.plane_id LEFT JOIN airline al ON al.id = f.airline_id WHERE f.id = 13 ORDER BY f.departure_time , f.id LIMIT 1;
show create table plane;
-- 为了重复执行脚本方便，先删旧触发器（如果存在）
DROP TRIGGER IF EXISTS trg_psc_after_insert;
DROP TRIGGER IF EXISTS trg_psc_after_update;
DROP TRIGGER IF EXISTS trg_psc_after_delete;

DELIMITER $$

/* ① plane_seat_capacity 新增：为该机型所有航班 upsert 对应舱位库存(total/available) */
CREATE TRIGGER trg_psc_after_insert
    AFTER INSERT ON plane_seat_capacity
    FOR EACH ROW
BEGIN
    /* 对所有使用该 plane 的航班做 upsert：
       - 如果不存在该 (flight_id, seat_type_id) 就插入：total=capacity, available=capacity, price=0.00(或沿用旧价)
       - 如果已存在就只更新 total/available
     */
    INSERT INTO flight_seat_inventory (flight_id, seat_type_id, total, available, price)
    SELECT
        f.id,
        NEW.seat_type_id,
        NEW.capacity,
        NEW.capacity,
        /* 若该航班该舱位已有记录则沿用其价格，否则给 0.00 作为占位 */
        COALESCE((
                     SELECT fsi2.price
                     FROM flight_seat_inventory fsi2
                     WHERE fsi2.flight_id = f.id AND fsi2.seat_type_id = NEW.seat_type_id
                     LIMIT 1
                 ), 0.00)
    FROM flight f
    WHERE f.plane_id = NEW.plane_id
    ON DUPLICATE KEY UPDATE
                         total     = VALUES(total),
                         available = VALUES(available);
END$$


/* ② plane_seat_capacity 更新：同步所有航班该舱位的 total/available */
CREATE TRIGGER trg_psc_after_update
    AFTER UPDATE ON plane_seat_capacity
    FOR EACH ROW
BEGIN
    /* 更新已有记录 */
    UPDATE flight_seat_inventory fsi
        JOIN flight f ON f.id = fsi.flight_id
    SET fsi.total = NEW.capacity,
        fsi.available = NEW.capacity
    WHERE f.plane_id = NEW.plane_id
      AND fsi.seat_type_id = NEW.seat_type_id;

    /* 同时为尚未建行的航班补齐（与 INSERT 触发器一致的 upsert 逻辑） */
    INSERT INTO flight_seat_inventory (flight_id, seat_type_id, total, available, price)
    SELECT
        f.id,
        NEW.seat_type_id,
        NEW.capacity,
        NEW.capacity,
        0.00
    FROM flight f
    WHERE f.plane_id = NEW.plane_id
      AND NOT EXISTS (
        SELECT 1
        FROM flight_seat_inventory x
        WHERE x.flight_id = f.id AND x.seat_type_id = NEW.seat_type_id
    );
END$$


/* ③ plane_seat_capacity 删除：将使用该机型的所有航班此舱位的 total/available 置 0
      （也可以选择直接 DELETE 对应行；此处保留 price 不动，便于日后恢复） */
CREATE TRIGGER trg_psc_after_delete
    AFTER DELETE ON plane_seat_capacity
    FOR EACH ROW
BEGIN
    UPDATE flight_seat_inventory fsi
        JOIN flight f ON f.id = fsi.flight_id
    SET fsi.total = 0,
        fsi.available = 0
    WHERE f.plane_id = OLD.plane_id
      AND fsi.seat_type_id = OLD.seat_type_id;
END$$

DELIMITER ;
show create table booking_order;
show create table booking_order_item;
show create table flight;
show create table airline;
show create table flight_seat_inventory;

INSERT INTO flight_seat_inventory (flight_id, seat_type_id, total, available, price) VALUES
-- 18 (206): 经济+商务
(18,1,150,150,780.00),(18,2,24,24,1880.00),
-- 19 (207): 经济+商务
(19,1,180,180,820.00),(19,2,28,28,1980.00),
-- 20 (208): 只经济（id%3=2? 此处示例按规则：id=20 %5=0 → 全舱）
(20,1,200,200,880.00),(20,2,32,32,2180.00),(20,3,12,12,3980.00),
-- 21 (206): 经济+商务
(21,1,150,150,780.00),(21,2,24,24,1880.00),
-- 22 (207): 经济+商务
(22,1,180,180,820.00),(22,2,28,28,1980.00),
-- 23 (208): 经济+商务
(23,1,200,200,880.00),(23,2,32,32,2180.00),
-- 24 (206): 只经济（24%3=0）
(24,1,150,150,780.00),
-- 25 (207): 全舱（25%5=0）
(25,1,180,180,820.00),(25,2,28,28,1980.00),(25,3,10,10,3580.00),
-- 26 (208): 经济+商务
(26,1,200,200,880.00),(26,2,32,32,2180.00),
-- 27 (206): 经济+商务
(27,1,150,150,780.00),(27,2,24,24,1880.00),
-- 28 (207): 经济+商务
(28,1,180,180,820.00),(28,2,28,28,1980.00),
-- 29 (208): 经济+商务
(29,1,200,200,880.00),(29,2,32,32,2180.00),
-- 30 (206): 全舱（30%5=0）
(30,1,150,150,780.00),(30,2,24,24,1880.00),(30,3,8,8,3380.00),
-- 31 (207): 经济+商务
(31,1,180,180,820.00),(31,2,28,28,1980.00),
-- 32 (208): 只经济（32%3≠0 且 32%5≠0，按默认：经+商；为展示多样，这里改为只经济）
(32,1,200,200,880.00),
-- 33 (206): 经济+商务
(33,1,150,150,780.00),(33,2,24,24,1880.00),
-- 34 (207): 经济+商务
(34,1,180,180,820.00),(34,2,28,28,1980.00),
-- 35 (208): 全舱（35%5=0）
(35,1,200,200,880.00),(35,2,32,32,2180.00),(35,3,12,12,3980.00),
-- 36 (206): 只经济（36%3=0）
(36,1,150,150,780.00),
-- 37 (207): 经济+商务
(37,1,180,180,820.00),(37,2,28,28,1980.00),
-- 38 (208): 经济+商务
(38,1,200,200,880.00),(38,2,32,32,2180.00),
-- 39 (206): 经济+商务
(39,1,150,150,780.00),(39,2,24,24,1880.00),
-- 40 (207): 全舱（40%5=0）
(40,1,180,180,820.00),(40,2,28,28,1980.00),(40,3,10,10,3580.00),
-- 41 (208): 经济+商务
(41,1,200,200,880.00),(41,2,32,32,2180.00),
-- 42 (206): 经济+商务
(42,1,150,150,780.00),(42,2,24,24,1880.00),
-- 43 (207): 经济+商务
(43,1,180,180,820.00),(43,2,28,28,1980.00),
-- 44 (208): 只经济（44%3≠0 且 44%5≠0，这里示例只经济）
(44,1,200,200,880.00),
-- 45 (206): 全舱（45%5=0）
(45,1,150,150,780.00),(45,2,24,24,1880.00),(45,3,8,8,3380.00),
-- 46 (207): 经济+商务
(46,1,180,180,820.00),(46,2,28,28,1980.00),
-- 47 (208): 经济+商务
(47,1,200,200,880.00),(47,2,32,32,2180.00),
-- 48 (206): 只经济（48%3=0）
(48,1,150,150,780.00),
-- 49 (207): 经济+商务
(49,1,180,180,820.00),(49,2,28,28,1980.00),
-- 50 (208): 全舱（50%5=0）
(50,1,200,200,880.00),(50,2,32,32,2180.00),(50,3,12,12,3980.00),
-- 51 (206): 经济+商务
(51,1,150,150,780.00),(51,2,24,24,1880.00),
-- 52 (207): 经济+商务
(52,1,180,180,820.00),(52,2,28,28,1980.00),
-- 53 (208): 经济+商务
(53,1,200,200,880.00),(53,2,32,32,2180.00),
-- 54 (206): 只经济（54%3=0）
(54,1,150,150,780.00),
-- 55 (207): 经济+商务
(55,1,180,180,820.00),(55,2,28,28,1980.00),
-- 56 (208): 经济+商务
(56,1,200,200,880.00),(56,2,32,32,2180.00),
-- 57 (206): 全舱（57%? 这里按示例给全舱以丰富数据）
(57,1,150,150,780.00),(57,2,24,24,1880.00),(57,3,8,8,3380.00);
INSERT INTO flight (id, name, plane_id, airline_id, departure_time, arrival_time) VALUES
                                                                                      (18, 'MU2001', 206, 5,  '2025-11-20 08:10:00', '2025-11-20 10:40:00'),
                                                                                      (19, 'CA6602', 207, 6,  '2025-11-20 09:25:00', '2025-11-20 12:00:00'),
                                                                                      (20, 'CZ7703', 208, 7,  '2025-11-20 13:15:00', '2025-11-20 15:55:00'),
                                                                                      (21, 'HU8804', 206, 8,  '2025-11-20 16:40:00', '2025-11-20 19:05:00'),
                                                                                      (22, 'MU2005', 207, 9,  '2025-11-21 07:30:00', '2025-11-21 10:00:00'),
                                                                                      (23, 'CA6606', 208, 10, '2025-11-21 10:20:00', '2025-11-21 12:45:00'),
                                                                                      (24, 'CZ7707', 206, 11, '2025-11-21 12:50:00', '2025-11-21 15:20:00'),
                                                                                      (25, 'HU8808', 207, 12, '2025-11-21 18:10:00', '2025-11-21 20:45:00'),
                                                                                      (26, 'MU2009', 208, 13, '2025-11-22 06:55:00', '2025-11-22 09:25:00'),
                                                                                      (27, 'CA6610', 206, 14, '2025-11-22 09:40:00', '2025-11-22 12:10:00'),
                                                                                      (28, 'CZ7711', 207, 15, '2025-11-22 14:05:00', '2025-11-22 16:35:00'),
                                                                                      (29, 'HU8812', 208, 16, '2025-11-22 19:20:00', '2025-11-22 21:55:00'),
                                                                                      (30, 'MU2013', 206, 17, '2025-11-23 07:10:00', '2025-11-23 09:45:00'),
                                                                                      (31, 'CA6614', 207, 5,  '2025-11-23 11:30:00', '2025-11-23 13:55:00'),
                                                                                      (32, 'CZ7715', 208, 6,  '2025-11-23 15:10:00', '2025-11-23 17:45:00'),
                                                                                      (33, 'HU8816', 206, 7,  '2025-11-23 20:25:00', '2025-11-23 22:50:00'),
                                                                                      (34, 'MU2017', 207, 8,  '2025-11-24 08:00:00', '2025-11-24 10:40:00'),
                                                                                      (35, 'CA6618', 208, 9,  '2025-11-24 09:50:00', '2025-11-24 12:20:00'),
                                                                                      (36, 'CZ7719', 206, 10, '2025-11-24 13:35:00', '2025-11-24 16:05:00'),
                                                                                      (37, 'HU8820', 207, 11, '2025-11-24 17:45:00', '2025-11-24 20:15:00'),
                                                                                      (38, 'MU2021', 208, 12, '2025-11-25 06:35:00', '2025-11-25 09:05:00'),
                                                                                      (39, 'CA6622', 206, 13, '2025-11-25 10:20:00', '2025-11-25 12:50:00'),
                                                                                      (40, 'CZ7723', 207, 14, '2025-11-25 14:15:00', '2025-11-25 16:45:00'),
                                                                                      (41, 'HU8824', 208, 15, '2025-11-25 18:30:00', '2025-11-25 21:00:00'),
                                                                                      (42, 'MU2025', 206, 16, '2025-11-26 07:25:00', '2025-11-26 09:55:00'),
                                                                                      (43, 'CA6626', 207, 17, '2025-11-26 11:05:00', '2025-11-26 13:35:00'),
                                                                                      (44, 'CZ7727', 208, 5,  '2025-11-26 15:55:00', '2025-11-26 18:25:00'),
                                                                                      (45, 'HU8828', 206, 6,  '2025-11-26 20:40:00', '2025-11-26 23:10:00'),
                                                                                      (46, 'MU2029', 207, 7,  '2025-11-27 08:20:00', '2025-11-27 10:50:00'),
                                                                                      (47, 'CA6630', 208, 8,  '2025-11-27 12:35:00', '2025-11-27 15:05:00'),
                                                                                      (48, 'CZ7731', 206, 9,  '2025-11-27 16:15:00', '2025-11-27 18:45:00'),
                                                                                      (49, 'HU8832', 207, 10, '2025-11-27 19:55:00', '2025-11-27 22:25:00'),
                                                                                      (50, 'MU2033', 208, 11, '2025-11-28 06:45:00', '2025-11-28 09:15:00'),
                                                                                      (51, 'CA6634', 206, 12, '2025-11-28 09:10:00', '2025-11-28 11:40:00'),
                                                                                      (52, 'CZ7735', 207, 13, '2025-11-28 13:25:00', '2025-11-28 15:55:00'),
                                                                                      (53, 'HU8836', 208, 14, '2025-11-28 17:10:00', '2025-11-28 19:40:00'),
                                                                                      (54, 'MU2037', 206, 15, '2025-11-29 07:05:00', '2025-11-29 09:35:00'),
                                                                                      (55, 'CA6638', 207, 16, '2025-11-29 10:50:00', '2025-11-29 13:20:00'),
                                                                                      (56, 'CZ7739', 208, 17, '2025-11-29 15:00:00', '2025-11-29 17:30:00'),
                                                                                      (57, 'HU8840', 206, 5,  '2025-11-29 19:30:00', '2025-11-29 22:00:00');


select * from flight_seat_inventory;
DELETE FROM flight_seat_inventory
WHERE price = 0 OR price IS NULL;
show tables ;
drop table flight_seat;