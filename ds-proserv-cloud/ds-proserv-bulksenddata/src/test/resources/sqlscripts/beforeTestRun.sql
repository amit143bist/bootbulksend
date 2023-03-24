INSERT INTO dbo.bulksendrecordlog
(recordid, recordtype, bulkbatchid, startdatetime, enddatetime, createdby, createddatetime, updatedby, updateddatetime) VALUES 
('1', 'landlordcp', 'TestBatchUUID', '2021-03-22 10:00:00', '2021-03-22 23:00:00', 'TestLoad', '2021-03-22 23:00:00', null, null),
('1', 'tenantcp', 'TestBatchUUID', '2021-03-22 10:00:00', '2021-03-22 23:00:00', 'TestLoad', '2021-03-22 23:00:00', null, null);