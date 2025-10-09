using pulsecharge.Models;
using pulsecharge.Mongo;
using MongoDB.Bson;
using MongoDB.Driver;

namespace pulsecharge.Repositories
{
    public class UserRepository
    {
        private readonly IMongoCollection<User> _users;
        public UserRepository(MongoContext context) 
        { 
            _users = context.Db.GetCollection<User>("users"); 
        }

        public Task<User?> FindByEmailAsync(string email) => _users.Find(u => u.Email == email).FirstOrDefaultAsync();
        public Task<User?> FindByNicAsync(string nic) => _users.Find(u => u.Nic == nic).FirstOrDefaultAsync();
        public Task<User?> FindByIdAsync(ObjectId id) => _users.Find(u => u.Id == id).FirstOrDefaultAsync();
        public Task CreateAsync(User u) => _users.InsertOneAsync(u);
        public Task ReplaceAsync(User u) => _users.ReplaceOneAsync(x => x.Id == u.Id, u);
        public IFindFluent<User, User> OwnersByNic(string? nic)
        {
            if (nic == null) return _users.Find(u => u.Role == "EvOwner");
            return _users.Find(Builders<User>.Filter.And(
                Builders<User>.Filter.Eq(u => u.Role, "EvOwner"),
                Builders<User>.Filter.Regex(u => u.Nic, nic)
            ));
        }
        public Task DeleteAsync(ObjectId id) => _users.DeleteOneAsync(u => u.Id == id);
        public IFindFluent<User, User> Operators() => _users.Find(u => u.Role == "StationOperator");

        public Task<List<User>> GetActiveOperatorsAsync()
        {
            var filter = Builders<User>.Filter.And(
                Builders<User>.Filter.Eq(u => u.Role, "StationOperator"),
                Builders<User>.Filter.Eq(u => u.Status, "active")
            );
            return _users.Find(filter).ToListAsync();
        }

        public Task<List<User>> GetAllOperatorsAsync()
        {
            var filter = Builders<User>.Filter.Eq(u => u.Role, "StationOperator");
            return _users.Find(filter).ToListAsync();
        }
    }
}