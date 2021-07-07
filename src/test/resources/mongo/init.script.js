conn = new Mongo();
db = conn.getDB("testdb");
db.createCollection("people");

db.people.insertOne({
    "_id" : "100",
    "firstname": "John",
    "lastname": "Doe",
    "status": "active",
    "lastLogin": new ISODate("2021-07-07T12:00:00Z")
})
