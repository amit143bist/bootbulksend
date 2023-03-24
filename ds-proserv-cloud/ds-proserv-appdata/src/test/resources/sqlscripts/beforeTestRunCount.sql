INSERT INTO ihdadb.dbo.customenvelopedata
(envelopeid, envtimestamp, envdate, senderidentifier, docdownloadstatusflag, docdownloadtimestamp, envprocessstatusflag, envprocessstartdatetime, envprocessenddatetime, createdby, createddatetime)
VALUES(NEWID(), GETDATE(),CAST( GETDATE() AS Date ), 'abc', 'COMPLETED', null, null, null, null, 'datacheck', GETDATE());

INSERT INTO ihdadb.dbo.customenvelopedata
(envelopeid, envtimestamp, envdate,  senderidentifier, docdownloadstatusflag, docdownloadtimestamp, envprocessstatusflag, envprocessstartdatetime, envprocessenddatetime, createdby, createddatetime)
VALUES(NEWID(), '2021-03-20T11:00:00','2021-03-20', 'abc', null, null, null, null, null, 'datacheck', GETDATE());

INSERT INTO ihdadb.dbo.customenvelopedata
(envelopeid, envtimestamp, envdate,  senderidentifier, docdownloadstatusflag, docdownloadtimestamp, envprocessstatusflag, envprocessstartdatetime, envprocessenddatetime, createdby, createddatetime)
VALUES(NEWID(), '2021-03-20T11:00:00', '2021-03-20','abc', 'COMPLETED', null, null, null, null, 'datacheck', GETDATE());

INSERT INTO ihdadb.dbo.customenvelopedata
(envelopeid, envtimestamp, envdate,  senderidentifier, docdownloadstatusflag, docdownloadtimestamp, envprocessstatusflag, envprocessstartdatetime, envprocessenddatetime, createdby, createddatetime)
VALUES(NEWID(), '2021-03-19T11:00:00', '2021-03-19','abc', null, null, null, null, null, 'datacheck', GETDATE());

INSERT INTO ihdadb.dbo.customenvelopedata
(envelopeid, envtimestamp, envdate,  senderidentifier, docdownloadstatusflag, docdownloadtimestamp, envprocessstatusflag, envprocessstartdatetime, envprocessenddatetime, createdby, createddatetime)
VALUES(NEWID(), '2021-03-19T13:00:00', '2021-03-19', 'abc', null, null, null, null, null, 'datacheck', GETDATE());

INSERT INTO ihdadb.dbo.customenvelopedata
(envelopeid, envtimestamp, envdate,  senderidentifier, docdownloadstatusflag, docdownloadtimestamp, envprocessstatusflag, envprocessstartdatetime, envprocessenddatetime, createdby, createddatetime)
VALUES(NEWID(), GETDATE(), CAST( GETDATE() AS Date ),'abc', 'COMPLETED', null, null, null, null, 'datacheck', GETDATE());

INSERT INTO ihdadb.dbo.customenvelopedata
(envelopeid, envtimestamp, envdate,  senderidentifier, docdownloadstatusflag, docdownloadtimestamp, envprocessstatusflag, envprocessstartdatetime, envprocessenddatetime, createdby, createddatetime)
VALUES(NEWID(), GETDATE(), CAST( GETDATE() AS Date ),'abc', 'COMPLETED', null, null, null, null, 'datacheck', GETDATE());