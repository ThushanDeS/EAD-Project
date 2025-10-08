
using MongoDB.Driver;

namespace pulsecharge.Mongo
{
    public class MongoContext
    {
        public IMongoDatabase Db { get; }
        public MongoContext(IConfiguration config)
        {
            var conn = config["Mongo:ConnectionString"] ?? "mongodb://localhost:27017";
            var dbName = config["Mongo:Database"] ?? "evcs_db";
            var client = new MongoClient(conn);
            Db = client.GetDatabase(dbName);
        }
    }
}
