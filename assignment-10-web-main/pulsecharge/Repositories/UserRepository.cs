/*
* File: UserRepository.cs
* Description: Provides data access methods for the User collection in MongoDB,
*              including CRUD operations and role-based queries for EV owners and operators.
*/

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
            _users = context.Db.GetCollection<User>("users");  // Initialize users collection
        }

        // Find a user by email
        public Task<User?> FindByEmailAsync(string email) => _users.Find(u => u.Email == email).FirstOrDefaultAsync();

        // Find a user by NIC
        public Task<User?> FindByNicAsync(string nic) => _users.Find(u => u.Nic == nic).FirstOrDefaultAsync();

        // Find a user by ID
        public Task<User?> FindByIdAsync(ObjectId id) => _users.Find(u => u.Id == id).FirstOrDefaultAsync();

        // Create a new user document
        public Task CreateAsync(User u) => _users.InsertOneAsync(u);

        // Replace an existing user document
        public Task ReplaceAsync(User u) => _users.ReplaceOneAsync(x => x.Id == u.Id, u);

        // Retrieve EV owners filtered by NIC (optional)
        public IFindFluent<User, User> OwnersByNic(string? nic)
        {
            if (nic == null) return _users.Find(u => u.Role == "EvOwner");
            return _users.Find(Builders<User>.Filter.And(
                Builders<User>.Filter.Eq(u => u.Role, "EvOwner"),
                Builders<User>.Filter.Regex(u => u.Nic, nic)
            ));
        }

        // Delete a user by ID
        public Task DeleteAsync(ObjectId id) => _users.DeleteOneAsync(u => u.Id == id);

        // Retrieve all station operators
        public IFindFluent<User, User> Operators() => _users.Find(u => u.Role == "StationOperator");

        // Retrieve all active station operators
        public Task<List<User>> GetActiveOperatorsAsync()
        {
            var filter = Builders<User>.Filter.And(
                Builders<User>.Filter.Eq(u => u.Role, "StationOperator"),
                Builders<User>.Filter.Eq(u => u.Status, "active")
            );
            return _users.Find(filter).ToListAsync();
        }

        // Retrieve all station operators (active and inactive)
        public Task<List<User>> GetAllOperatorsAsync()
        {
            var filter = Builders<User>.Filter.Eq(u => u.Role, "StationOperator");
            return _users.Find(filter).ToListAsync();
        }
    }
}
