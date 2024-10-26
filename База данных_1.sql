CREATE TABLE providers(
ID BIGINT NOT NULL PRIMARY KEY,
Name VARCHAR(100) NOT NULL,
Email VARCHAR(100) NOT NULL,
ContactPerson VARCHAR(100) NOT NULL,
Comments TEXT
);

CREATE TABLE SouvenirProcurements(
ID BIGINT NOT NULL PRIMARY KEY,
CONSTRAINT idProvider FOREIGN KEY(id) REFERENCES Providers(ID),
DATA DATE NOT NULL,
CONSTRAINT idStatus FOREIGN KEY(ID) REFERENCES ProcurementStatuses(ID)
);

CREATE TABLE Colors(
id BIGINT NOT NULL PRIMARY KEY,
name VARCHAR(200) NOT NULL);
CREATE TABLE Colors(
id BIGINT NOT NULL PRIMARY KEY,
name VARCHAR(200) NOT NULL
);


CREATE TABLE SouvenirMaterials(
ID INT NOT NULL PRIMARY KEY,
NAME VARCHAR(200) NOT NULL
);

CREATE TABLE SouvenirsCategories(
ID BIGINT NOT NULL PRIMARY KEY,
idParent BIGINT,
name VARCHAR(100) NOT NULL
);


CREATE TABLE ApplicationMetods(
ID INT NOT NULL PRIMARY KEY,
NAME VARCHAR(200) NOT NULL 
);


CREATE TABLE SouvenirsCategories(
ID BIGINT NOT NULL PRIMARY KEY,
NAME VARCHAR(100) NOT NULL
);

CREATE TABLE Souvenirs(
ID BIGINT NOT NULL PRIMARY KEY,
URL VARCHAR(100) NOT NULL,
SHORTNAME VARCHAR(150) NOT NULL,
NAME VARCHAR(200) NOT NULL,
Description VARCHAR(2500) NOT NULL,
Rating SMALLINT NOT NULL,
idCategory BIGINT NOT NULL,
FOREIGN KEY (idCategory) REFERENCES SouvenirsCategories(ID),
idColor BIGINT NOT NULL,
FOREIGN KEY (idColor) REFERENCES colors(ID),
size varchar(150) NOT NULL,
idMaterial INT NOT NULL,
FOREIGN KEY (idMaterial) REFERENCES SouvenirMaterials(id),
Weight DECIMAL(10,2),
OTypics VARCHAR(10),
PICSSIZE VARCHAR(20),
IdApplicMetod INT NOT NULL,
FOREIGN KEY (IdApplicMetod) REFERENCES ApplicationMetods(ID), 
AllCategories VARCHAR(150) NOT NULL,
DEALERPRICE DECIMAL(10,2) NOT NULL,
PRICE DECIMAL(10,2) NOT NULL,
COMMENTS VARCHAR(1000)
);

CREATE TABLE ProcurementSouvenirs(
ID BIGINT NOT NULL PRIMARY KEY,
idSouvenir BIGINT NOT NULL,
FOREIGN KEY (idSouvenir) REFERENCES Souvenirs(ID),
IdProcurement BIGINT NOT NULL,
FOREIGN KEY (IdProcurement) REFERENCES SouvenirProcurements(ID),
Amount BIGINT NOT NULL,
PRICE DECIMAL(10,2)
);

CREATE TABLE SouvenirStores(
ID BIGINT NOT NULL PRIMARY KEY,
IdSouvenir BIGINT NOT NULL,
FOREIGN KEY (idSouvenir) REFERENCES SOUVENIRS(ID),
IdProcurement BIGINT NOT NULL,
FOREIGN KEY (IdProcurement) REFERENCES SouvenirProcurements(ID),
AMOUNT BIGINT NOT NULL,
COMMENTS VARCHAR(1000)
);

CREATE TABLE ProcurementStatuses(
id INT NOT NULL PRIMARY KEY,
NAME varchar(30) NOT NULL
);

INSERT INTO SouvenirProcurements (id, data, idStatus) VALUES
        (1, 1, '2024-10-01', 1),
        (2, 2, '2024-10-02', 2),
        (3, 3, '2024-10-03', 1),
        (4, 4, '2024-10-04', 3),
        (5, 5, '2024-10-05', 2);


INSERT INTO ProcurementStatuses (id, name) VALUES
        (1, 'Pending'),
        (2, 'Completed'),
        (3, 'Cancelled'),
        (4, 'In Progress'),
        (5, 'On Hold');


INSERT INTO providers (id, name, email, contactperson, comments) VALUES
            (1, 'Karam', 'karam@yandex.com', '9256881341', NULL),
            (2, 'Dima', 'dima@yandex.com', '7129312', NULL),
            (3, 'Alex', 'alex@yandex.com', '29312121', NULL),
            (4, 'Alena', 'Alena@yandex.com', '291931', NULL),
            (5, 'John', 'john@yandex.com', '1231231231', NULL);




INSERT INTO ProcurementSouvenirs (id, idSouvenir, idProcurement, amount, price) VALUES
            (1, 15279, 1, 5, 9990.00),
            (2, 15280, 2, 10, 12290.00),
            (3, 15283, 3, 3, 7870.00),
            (4, 15284, 4, 8, 6051.00),
            (5, 15288, 5, 2, 123.00);


INSERT INTO SouvenirStores (id, idSouvenir, idprocurement, amount, comments) VALUES
        (1, 15279, 1, 5, 'Initial stock'),
        (2, 15280, 2, 10, 'Restocked'),
        (3, 15283, 3, 3, 'Limited edition'),
        (4, 15284, 4, 8, 'Best seller'),
        (5, 15288, 5, 2, 'Clearance item');
--1. Запрос на выборку сувениров по материалу
SELECT s.id, s.name, s.description, s.price, m.name AS material_name
FROM Souvenirs s
         JOIN SouvenirMaterials m ON s.idMaterial = m.id
WHERE m.name = 'металл';
--2. Запрос на выборку поставок сувениров за промежуток времени
SELECT sp.id, sp.data, sp.idProvider, p.name AS provider_name
FROM SouvenirProcurements sp
         JOIN Providers p ON sp.idProvider = p.id
WHERE sp.data BETWEEN '2024-01-01' AND '2024-10-05';

--3. Запрос на выборку сувениров по категориям и отсортировать по популярности от самого непопулярного
SELECT s.id, s.name, s.description, s.rating, c.name AS category_name
FROM Souvenirs s
         JOIN SouvenirsCategories c ON s.idCategory = c.id
ORDER BY s.rating ASC;

--4. Запрос на выборку всех поставщиков, поставляющих категорию товара
SELECT DISTINCT p.id, p.name, p.email, p.contactperson
FROM Providers p
         JOIN SouvenirProcurements sp ON p.id = sp.idprovider
         JOIN ProcurementSouvenirs ps ON sp.id = ps.idProcurement
         JOIN Souvenirs s ON ps.idSouvenir = s.id
         JOIN SouvenirsCategories c ON s.idCategory = c.id
WHERE c.name = 'Органайзеры';

--5. Запрос на выборку поставок сувениров за промежуток времени и отсортировать по статусу
SELECT sp.id, sp.data, ps.name AS status_name, sp.idProvider
FROM SouvenirProcurements sp
JOIN ProcurementStatuses ps ON sp.idStatus = ps.id
WHERE sp.data BETWEEN '2024-10-01' AND '2024-10-02'
ORDER BY ps.name;


--6. Создать объект для вывода категорий, в зависимости от выбранной
CREATE OR REPLACE FUNCTION get_categories_by_name(category_name VARCHAR)
    RETURNS TABLE (id BIGINT, idparent BIGINT, name VARCHAR) AS $$
BEGIN
    RETURN QUERY
    SELECT sc.id, sc.idparent, sc.name
    FROM SouvenirsCategories sc
    WHERE sc.name = category_name;
END;
$$ LANGUAGE plpgsql;

SELECT * FROM get_categories_by_name('Органайзеры');


--7.	Создать объект для проверки правильности занесения данных в таблицу SouvenirsCategories
SELECT *
FROM SouvenirsCategories
WHERE name = 'Органайзеры';

CREATE OR REPLACE FUNCTION check_souvenirs_categories()
    RETURNS TABLE (id BIGINT, idparent BIGINT, name VARCHAR) AS $$
BEGIN
    RETURN QUERY
        SELECT sc.id, sc.idparent, sc.name
        FROM souvenirscategories sc
        WHERE sc.name = 'Органайзеры'; 
END;
$$ LANGUAGE plpgsql;
SELECT * FROM check_souvenirs_categories();


--8.	Создать объект оповещения пользователя при отсутствии поставок товаров, отсутствующих на складе или количество которых меньше чем 50 шт.
CREATE OR REPLACE FUNCTION alert_low_stock()
    RETURNS TABLE (SouvenirID BIGINT, SouvenirName VARCHAR, Amount BIGINT) AS $$
BEGIN
    RETURN QUERY
        SELECT s.id, s.name, ss.amount
        FROM souvenirs s
                 LEFT JOIN souvenirstores ss ON s.id = ss.idsouvenir
        WHERE ss.idsouvenir IS NOT NULL OR ss.amount < 50;
END;
$$ LANGUAGE plpgsql;

SELECT * FROM alert_low_stock();
