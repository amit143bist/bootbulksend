delete from ihdadb.dbo.drawapplication
delete from ihdadb.dbo.dsenvelope
delete from ihdadb.dbo.dsrecipient
delete from ihdadb.dbo.dstab
delete from ihdadb.dbo.bulksendenvelopelog 
delete from ihdadb.dbo.bulksendfailurelog 
delete from ihdadb.dbo.bulksendlog 
delete from ihdadb.dbo.coreprocessfailurelog 
delete from ihdadb.dbo.coreconcurrentprocesslog 
delete from ihdadb.dbo.corescheduledbatchlog 
delete from ihdadb.dbo.dsexception 


INSERT INTO ihdadb.dbo.drawapplication
(applicationid, triggerenvelopeid, bridgeenvelopeid, bulkbatchid, programtype, drawreference, languagecode, agentcode, duplicaterecord, applicationstatus, createdby, createddatetime, updatedby, updateddatetime)
VALUES('3cbded88-a65b-4893-aeb6-1f15c1aa1e09', '963db0b3-b343-42a9-8eb4-46aa877064b8', NULL, NULL, 'ERA', NULL, 'EN', NULL, NULL, 'APP_INITIATOR_FINISHED', 'docusignuser', '2020-08-02 16:29:28.830', NULL, NULL);

INSERT INTO ihdadb.dbo.dsenvelope
(envelopeid, envelopesubject, status, sentdatetime, delivereddatetime, completeddatetime, declineddatetime, senderemail, sendername, terminalreason, timezone, timezoneoffset, timegenerated, createdby, createddatetime, updatedby, updateddatetime)
VALUES('963db0b3-b343-42a9-8eb4-46aa877064b8', 'ERA Tenant Application - Tenant User', 'Completed', '2020-08-02 09:09:46.077', '2020-08-02 09:09:59.873', '2020-08-02 09:12:49.310', NULL, 'barpe061+ihda@gmail.com', 'Pedro Barroso', NULL, 'Pacific Standard Time', -7, '2020-08-02 09:28:04.117', 'docusignuser', '2020-08-02 16:29:27.467', NULL, NULL);

INSERT INTO ihdadb.dbo.dsrecipient
(recipientid, envelopeid, status, routingorder, recipientemail, recipientname, declinereason, recipientipaddress, createdby, createddatetime, updatedby, updateddatetime)
VALUES('c2276d2e-0a0d-4982-b421-8454a699b2f1', '963db0b3-b343-42a9-8eb4-46aa877064b8', 'COMPLETED', 1, 'frankftsai.customer+tenant@gmail.com', 'Tenant User', NULL, '12.148.184.130', 'docusignuser', '2020-08-02 16:29:27.430', NULL, NULL);

INSERT INTO ihdadb.dbo.dstab
(id, envelopeid, recipientid, tablabel, tabname, tabvalue, taboriginalvalue, createdby, createddatetime, updatedby, updateddatetime)
VALUES('00d93a3a-b480-40b8-9a31-342f596d4b39', '963db0b3-b343-42a9-8eb4-46aa877064b8', 'c2276d2e-0a0d-4982-b421-8454a699b2f1', 'tenantfirstname', '', 'fname', '', 'docusignuser', '2020-08-02 16:29:27.383', NULL, NULL);
INSERT INTO ihdadb.dbo.dstab
(id, envelopeid, recipientid, tablabel, tabname, tabvalue, taboriginalvalue, createdby, createddatetime, updatedby, updateddatetime)
VALUES('1a4169be-9a16-480f-9632-bdd597fae651', '963db0b3-b343-42a9-8eb4-46aa877064b8', 'c2276d2e-0a0d-4982-b421-8454a699b2f1', 'propertyownership', 'nopropertyownership', 'yespropertyownership', '', 'docusignuser', '2020-08-02 16:29:27.377', NULL, NULL);
INSERT INTO ihdadb.dbo.dstab
(id, envelopeid, recipientid, tablabel, tabname, tabvalue, taboriginalvalue, createdby, createddatetime, updatedby, updateddatetime)
VALUES('23bd316c-9373-4277-a630-f57b5737eef1', '963db0b3-b343-42a9-8eb4-46aa877064b8', 'c2276d2e-0a0d-4982-b421-8454a699b2f1', 'race', 'asian', 'americanindianoralaska', '', 'docusignuser', '2020-08-02 16:29:27.387', NULL, NULL);
INSERT INTO ihdadb.dbo.dstab
(id, envelopeid, recipientid, tablabel, tabname, tabvalue, taboriginalvalue, createdby, createddatetime, updatedby, updateddatetime)
VALUES('27d12aeb-e0f4-447f-8946-8294ed8cb04a', '963db0b3-b343-42a9-8eb4-46aa877064b8', 'c2276d2e-0a0d-4982-b421-8454a699b2f1', 'rentalhousingassistance', 'Yes', 'Yes', '', 'docusignuser', '2020-08-02 16:29:27.377', NULL, NULL);
INSERT INTO ihdadb.dbo.dstab
(id, envelopeid, recipientid, tablabel, tabname, tabvalue, taboriginalvalue, createdby, createddatetime, updatedby, updateddatetime)
VALUES('3b34cf0d-2252-4fe8-9e40-577c5231860b', '963db0b3-b343-42a9-8eb4-46aa877064b8', 'c2276d2e-0a0d-4982-b421-8454a699b2f1', 'templatetype', '', 'TENANT', 'TENANT', 'docusignuser', '2020-08-02 16:29:27.380', NULL, NULL);
INSERT INTO ihdadb.dbo.dstab
(id, envelopeid, recipientid, tablabel, tabname, tabvalue, taboriginalvalue, createdby, createddatetime, updatedby, updateddatetime)
VALUES('43ba4085-68bc-4bf6-96e4-c076ecc270f2', '963db0b3-b343-42a9-8eb4-46aa877064b8', 'c2276d2e-0a0d-4982-b421-8454a699b2f1', 'agentcode', '', '', '', 'docusignuser', '2020-08-02 16:29:27.380', NULL, NULL);
INSERT INTO ihdadb.dbo.dstab
(id, envelopeid, recipientid, tablabel, tabname, tabvalue, taboriginalvalue, createdby, createddatetime, updatedby, updateddatetime)
VALUES('59322027-1804-46fb-9ced-d41b282e2d8b', '963db0b3-b343-42a9-8eb4-46aa877064b8', 'c2276d2e-0a0d-4982-b421-8454a699b2f1', 'hispaniclatinogroup', 'otherhispaniclatino', 'mexican', '', 'docusignuser', '2020-08-02 16:29:27.383', NULL, NULL);
INSERT INTO ihdadb.dbo.dstab
(id, envelopeid, recipientid, tablabel, tabname, tabvalue, taboriginalvalue, createdby, createddatetime, updatedby, updateddatetime)
VALUES('5da9af2e-612f-4232-8186-cfa20d9a387b', '963db0b3-b343-42a9-8eb4-46aa877064b8', 'c2276d2e-0a0d-4982-b421-8454a699b2f1', 'landlordemail', 'Enter valid landlord email address', 'frankftsai.customer+landlord@gmail.com', '', 'docusignuser', '2020-08-02 16:29:27.377', NULL, NULL);
INSERT INTO ihdadb.dbo.dstab
(id, envelopeid, recipientid, tablabel, tabname, tabvalue, taboriginalvalue, createdby, createddatetime, updatedby, updateddatetime)
VALUES('5e1120d0-e940-4640-8512-7ec3e6f15e0d', '963db0b3-b343-42a9-8eb4-46aa877064b8', 'c2276d2e-0a0d-4982-b421-8454a699b2f1', '#HREF_Link2', 'https://era.ihda.org/uploads/Instructions.pdf', 'click here.', 'click here.', 'docusignuser', '2020-08-02 16:29:27.383', NULL, NULL);
INSERT INTO ihdadb.dbo.dstab
(id, envelopeid, recipientid, tablabel, tabname, tabvalue, taboriginalvalue, createdby, createddatetime, updatedby, updateddatetime)
VALUES('5f3b0c52-d61b-4c0f-add1-2560b47c52fb', '963db0b3-b343-42a9-8eb4-46aa877064b8', 'c2276d2e-0a0d-4982-b421-8454a699b2f1', 'involuntarylossincome', 'Yes', 'Yes', '', 'docusignuser', '2020-08-02 16:29:27.377', NULL, NULL);
INSERT INTO ihdadb.dbo.dstab
(id, envelopeid, recipientid, tablabel, tabname, tabvalue, taboriginalvalue, createdby, createddatetime, updatedby, updateddatetime)
VALUES('5fcafcf7-4993-49aa-bbd2-c250055404d2', '963db0b3-b343-42a9-8eb4-46aa877064b8', 'c2276d2e-0a0d-4982-b421-8454a699b2f1', 'landlordname', 'Enter landlord name here, only letters', 'landlord name', '', 'docusignuser', '2020-08-02 16:29:27.383', NULL, NULL);
INSERT INTO ihdadb.dbo.dstab
(id, envelopeid, recipientid, tablabel, tabname, tabvalue, taboriginalvalue, createdby, createddatetime, updatedby, updateddatetime)
VALUES('62388c98-7a7a-4d0a-8a37-4434f79824ec', '963db0b3-b343-42a9-8eb4-46aa877064b8', 'c2276d2e-0a0d-4982-b421-8454a699b2f1', 'photoIddonotmatch', '', '', '', 'docusignuser', '2020-08-02 16:29:27.387', NULL, NULL);
INSERT INTO ihdadb.dbo.dstab
(id, envelopeid, recipientid, tablabel, tabname, tabvalue, taboriginalvalue, createdby, createddatetime, updatedby, updateddatetime)
VALUES('6384342a-2370-4125-9433-4a19d7a17b1c', '963db0b3-b343-42a9-8eb4-46aa877064b8', 'c2276d2e-0a0d-4982-b421-8454a699b2f1', 'tenantaddressstreet', '', 'street', '', 'docusignuser', '2020-08-02 16:29:27.373', NULL, NULL);
INSERT INTO ihdadb.dbo.dstab
(id, envelopeid, recipientid, tablabel, tabname, tabvalue, taboriginalvalue, createdby, createddatetime, updatedby, updateddatetime)
VALUES('6907bb89-e94a-42d8-87a7-713ec85d0d0c', '963db0b3-b343-42a9-8eb4-46aa877064b8', 'c2276d2e-0a0d-4982-b421-8454a699b2f1', 'landlordphone', 'Enter Landlord phone number', '123-123-1234', '', 'docusignuser', '2020-08-02 16:29:27.383', NULL, NULL);
INSERT INTO ihdadb.dbo.dstab
(id, envelopeid, recipientid, tablabel, tabname, tabvalue, taboriginalvalue, createdby, createddatetime, updatedby, updateddatetime)
VALUES('6b43e4c2-6d77-429d-856c-33f9cb06cd61', '963db0b3-b343-42a9-8eb4-46aa877064b8', 'c2276d2e-0a0d-4982-b421-8454a699b2f1', 'tribename', 'Enter tribe', 'was', '', 'docusignuser', '2020-08-02 16:29:27.377', NULL, NULL);
INSERT INTO ihdadb.dbo.dstab
(id, envelopeid, recipientid, tablabel, tabname, tabvalue, taboriginalvalue, createdby, createddatetime, updatedby, updateddatetime)
VALUES('76e7da58-dc0d-4128-aa9f-b4d038f26e90', '963db0b3-b343-42a9-8eb4-46aa877064b8', 'c2276d2e-0a0d-4982-b421-8454a699b2f1', 'tenantaddressunit', '', 'unit', '', 'docusignuser', '2020-08-02 16:29:27.383', NULL, NULL);
INSERT INTO ihdadb.dbo.dstab
(id, envelopeid, recipientid, tablabel, tabname, tabvalue, taboriginalvalue, createdby, createddatetime, updatedby, updateddatetime)
VALUES('99419b76-7112-4dbf-aea2-a55033006303', '963db0b3-b343-42a9-8eb4-46aa877064b8', 'c2276d2e-0a0d-4982-b421-8454a699b2f1', 'ethnicitygroup', 'hispanicorlatino', 'hispanicorlatino', '', 'docusignuser', '2020-08-02 16:29:27.377', NULL, NULL);
INSERT INTO ihdadb.dbo.dstab
(id, envelopeid, recipientid, tablabel, tabname, tabvalue, taboriginalvalue, createdby, createddatetime, updatedby, updateddatetime)
VALUES('9b77cad0-e900-42a1-b13d-db4bc69b2605', '963db0b3-b343-42a9-8eb4-46aa877064b8', 'c2276d2e-0a0d-4982-b421-8454a699b2f1', 'languagecode', '', 'EN', 'EN', 'docusignuser', '2020-08-02 16:29:27.380', NULL, NULL);
INSERT INTO ihdadb.dbo.dstab
(id, envelopeid, recipientid, tablabel, tabname, tabvalue, taboriginalvalue, createdby, createddatetime, updatedby, updateddatetime)
VALUES('a01ea3af-38bb-4189-9d31-b5e33a4e0eff', '963db0b3-b343-42a9-8eb4-46aa877064b8', 'c2276d2e-0a0d-4982-b421-8454a699b2f1', 'tenantaddresscounty', 'Adams;Alexander;Bond;Boone;Brown;Bureau;Calhoun;Carroll;Cass;Champaign;Christian;Clark;Clay;Clinton;Coles;Cook;Crawford;Cumberland;DeKalb;De Witt;Douglas;DuPage;Edgar;Edwards;Effingham;Fayette;Ford;Franklin;Fulton;Gallatin;Greene;Grundy;Hamilton;Hancock;Hardin;Henderson;Henry;Iroquois;Jackson;Jasper;Jefferson;Jersey;Jo Daviess;Johnson;Kane;Kankakee;Kendall;Knox;La Salle;Lake;Lawrence;Lee;Livingston;Logan;McDonough;McHenry;McLean;Macon;Macoupin;Madison;Marion;Marshall;Mason;Massac;Menard;Mercer;Monroe;Montgomery;Morgan;Moultrie;Ogle;Peoria;Perry;Piatt;Pike;Pope;Pulaski;Putnam;Randolph;Richland;Rock Island;Saline;Sangamon;Schuyler;Scott;Shelby;St. Clair;Stark;Stephenson;Tazewell;Union;Vermilion;Wabash;Warren;Washington;Wayne;White;Whiteside;Will;Williamson;Winnebago;Woodford', 'Union', '', 'docusignuser', '2020-08-02 16:29:27.387', NULL, NULL);
INSERT INTO ihdadb.dbo.dstab
(id, envelopeid, recipientid, tablabel, tabname, tabvalue, taboriginalvalue, createdby, createddatetime, updatedby, updateddatetime)
VALUES('a50d7109-f81b-4ba0-9305-a05602efcc64', '963db0b3-b343-42a9-8eb4-46aa877064b8', 'c2276d2e-0a0d-4982-b421-8454a699b2f1', 'programtype', '', 'ERA', 'ERA', 'docusignuser', '2020-08-02 16:29:27.387', NULL, NULL);
INSERT INTO ihdadb.dbo.dstab
(id, envelopeid, recipientid, tablabel, tabname, tabvalue, taboriginalvalue, createdby, createddatetime, updatedby, updateddatetime)
VALUES('a5f96d69-eb5e-49b3-af20-dc0ed66ac99a', '963db0b3-b343-42a9-8eb4-46aa877064b8', 'c2276d2e-0a0d-4982-b421-8454a699b2f1', 'tenantphonenumber', 'Enter primary phone number', '123-123-1234', '', 'docusignuser', '2020-08-02 16:29:27.387', NULL, NULL);
INSERT INTO ihdadb.dbo.dstab
(id, envelopeid, recipientid, tablabel, tabname, tabvalue, taboriginalvalue, createdby, createddatetime, updatedby, updateddatetime)
VALUES('a961d341-7cbb-4508-9d7a-890c7a1a7838', '963db0b3-b343-42a9-8eb4-46aa877064b8', 'c2276d2e-0a0d-4982-b421-8454a699b2f1', 'DateSigned', 'DateSigned', '8/2/2020', '', 'docusignuser', '2020-08-02 16:29:27.387', NULL, NULL);
INSERT INTO ihdadb.dbo.dstab
(id, envelopeid, recipientid, tablabel, tabname, tabvalue, taboriginalvalue, createdby, createddatetime, updatedby, updateddatetime)
VALUES('b76f360c-0d08-4659-99ed-ecf93306b2e9', '963db0b3-b343-42a9-8eb4-46aa877064b8', 'c2276d2e-0a0d-4982-b421-8454a699b2f1', 'tenantmiddleinitial', '', 'm', '', 'docusignuser', '2020-08-02 16:29:27.387', NULL, NULL);
INSERT INTO ihdadb.dbo.dstab
(id, envelopeid, recipientid, tablabel, tabname, tabvalue, taboriginalvalue, createdby, createddatetime, updatedby, updateddatetime)
VALUES('b8064b59-05e8-459e-97ba-187f74ce35e8', '963db0b3-b343-42a9-8eb4-46aa877064b8', 'c2276d2e-0a0d-4982-b421-8454a699b2f1', 'monthsbehindinrental', 'Number number of payments missed', '1', '', 'docusignuser', '2020-08-02 16:29:27.310', NULL, NULL);
INSERT INTO ihdadb.dbo.dstab
(id, envelopeid, recipientid, tablabel, tabname, tabvalue, taboriginalvalue, createdby, createddatetime, updatedby, updateddatetime)
VALUES('c47a7722-0a0f-48c2-b2dc-8a98da665255', '963db0b3-b343-42a9-8eb4-46aa877064b8', 'c2276d2e-0a0d-4982-b421-8454a699b2f1', 'tenantlastname', '', 'lname', '', 'docusignuser', '2020-08-02 16:29:27.377', NULL, NULL);
INSERT INTO ihdadb.dbo.dstab
(id, envelopeid, recipientid, tablabel, tabname, tabvalue, taboriginalvalue, createdby, createddatetime, updatedby, updateddatetime)
VALUES('c4ff45b8-73a9-4a05-a644-5eacd1263f08', '963db0b3-b343-42a9-8eb4-46aa877064b8', 'c2276d2e-0a0d-4982-b421-8454a699b2f1', 'tenantaddresscity', '', 'city', '', 'docusignuser', '2020-08-02 16:29:27.387', NULL, NULL);
INSERT INTO ihdadb.dbo.dstab
(id, envelopeid, recipientid, tablabel, tabname, tabvalue, taboriginalvalue, createdby, createddatetime, updatedby, updateddatetime)
VALUES('e79b3970-c968-465e-a329-f65ea97384d8', '963db0b3-b343-42a9-8eb4-46aa877064b8', 'c2276d2e-0a0d-4982-b421-8454a699b2f1', 'tenantaddresszip', '', '12345', '', 'docusignuser', '2020-08-02 16:29:27.380', NULL, NULL);
INSERT INTO ihdadb.dbo.dstab
(id, envelopeid, recipientid, tablabel, tabname, tabvalue, taboriginalvalue, createdby, createddatetime, updatedby, updateddatetime)
VALUES('ec36060c-3e05-4d95-bc39-cffe023350b3', '963db0b3-b343-42a9-8eb4-46aa877064b8', 'c2276d2e-0a0d-4982-b421-8454a699b2f1', '#HREF_Link4', 'https://era.ihda.org/', 'IHDA website here', 'IHDA website here', 'docusignuser', '2020-08-02 16:29:27.387', NULL, NULL);
INSERT INTO ihdadb.dbo.dstab
(id, envelopeid, recipientid, tablabel, tabname, tabvalue, taboriginalvalue, createdby, createddatetime, updatedby, updateddatetime)
VALUES('ed3ead63-ae05-429c-9693-f90465959243', '963db0b3-b343-42a9-8eb4-46aa877064b8', 'c2276d2e-0a0d-4982-b421-8454a699b2f1', '#HREF_Link3', 'https://era.ihda.org/uploads/Instructions.pdf', 'click here.', 'click here.', 'docusignuser', '2020-08-02 16:29:27.387', NULL, NULL);
INSERT INTO ihdadb.dbo.dstab
(id, envelopeid, recipientid, tablabel, tabname, tabvalue, taboriginalvalue, createdby, createddatetime, updatedby, updateddatetime)
VALUES('f1b5805d-3ced-4f5c-947f-763736bdefa2', '963db0b3-b343-42a9-8eb4-46aa877064b8', 'c2276d2e-0a0d-4982-b421-8454a699b2f1', 'numberofresidents', 'Enter number of people in your household', '1', '', 'docusignuser', '2020-08-02 16:29:27.377', NULL, NULL);
INSERT INTO ihdadb.dbo.dstab
(id, envelopeid, recipientid, tablabel, tabname, tabvalue, taboriginalvalue, createdby, createddatetime, updatedby, updateddatetime)
VALUES('f2ef8d79-b974-4697-8c53-10ddc0d337fc', '963db0b3-b343-42a9-8eb4-46aa877064b8', 'c2276d2e-0a0d-4982-b421-8454a699b2f1', '#HREF_Link1', 'https://era.ihda.org/uploads/FAQ.pdf', 'IHDA website', 'IHDA website', 'docusignuser', '2020-08-02 16:29:27.387', NULL, NULL);
INSERT INTO ihdadb.dbo.dstab
(id, envelopeid, recipientid, tablabel, tabname, tabvalue, taboriginalvalue, createdby, createddatetime, updatedby, updateddatetime)
VALUES('f33b38fb-e126-41a2-b327-dbf024bf186b', '963db0b3-b343-42a9-8eb4-46aa877064b8', 'c2276d2e-0a0d-4982-b421-8454a699b2f1', 'tenantdob', 'Birth Date: mm/dd/yyyy', '08/02/2020', '', 'docusignuser', '2020-08-02 16:29:27.383', NULL, NULL);
INSERT INTO ihdadb.dbo.dstab
(id, envelopeid, recipientid, tablabel, tabname, tabvalue, taboriginalvalue, createdby, createddatetime, updatedby, updateddatetime)
VALUES('f5dc0a1c-125b-4731-9af8-5fd4133b9cab', '963db0b3-b343-42a9-8eb4-46aa877064b8', 'c2276d2e-0a0d-4982-b421-8454a699b2f1', 'gendergroup', 'female', 'female', '', 'docusignuser', '2020-08-02 16:29:27.377', NULL, NULL);
INSERT INTO ihdadb.dbo.dstab
(id, envelopeid, recipientid, tablabel, tabname, tabvalue, taboriginalvalue, createdby, createddatetime, updatedby, updateddatetime)
VALUES('fe18b786-0576-4712-8a11-ade92a0c8b97', '963db0b3-b343-42a9-8eb4-46aa877064b8', 'c2276d2e-0a0d-4982-b421-8454a699b2f1', 'covidrentalassistance', 'Yes', 'Yes', '', 'docusignuser', '2020-08-02 16:29:27.387', NULL, NULL);