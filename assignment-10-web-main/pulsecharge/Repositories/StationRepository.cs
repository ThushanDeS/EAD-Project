using pulsecharge.Models;
using pulsecharge.Mongo;
using MongoDB.Bson;
using MongoDB.Driver;

namespace pulsecharge.Repositories
{
    public class StationRepository
    {
        private readonly IMongoCollection<Station> _col;
        public StationRepository(MongoContext ctx) { _col = ctx.Db.GetCollection<Station>("stations"); }

        public Task CreateAsync(Station s) => _col.InsertOneAsync(s);
        public Task<Station?> FindAsync(ObjectId id) => _col.Find(s => s.Id == id).FirstOrDefaultAsync();
        public Task ReplaceAsync(Station s) => _col.ReplaceOneAsync(x => x.Id == s.Id, s);
        public IFindFluent<Station, Station> Query(bool? active, string? type)
        {
            var filter = Builders<Station>.Filter.Empty;
            if (active != null) filter &= Builders<Station>.Filter.Eq(s => s.Active, active);
            if (type != null) filter &= Builders<Station>.Filter.Eq(s => s.Type, type);
            return _col.Find(filter);
        }
        public IMongoCollection<Station> Collection => _col;

        public Task<Station?> FindByOperatorIdAsync(ObjectId operatorId)
        {
            return _col.Find(s => s.OperatorIds.Contains(operatorId)).FirstOrDefaultAsync();
        }

    }
}
