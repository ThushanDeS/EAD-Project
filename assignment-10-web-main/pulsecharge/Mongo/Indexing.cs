/*
* File: Indexing.cs
* Description: Manages MongoDB index creation for PulseCharge collections to ensure
*              optimized query performance and data integrity.
*/

using pulsecharge.Models;
using MongoDB.Driver;

namespace pulsecharge.Mongo
{
    public class Indexing
    {
        private readonly MongoContext _ctx;
        public Indexing(MongoContext ctx) { _ctx = ctx; }  // Initialize MongoDB context

        // Create necessary indexes for all collections
        public void EnsureAll()
        {
            var users = _ctx.Db.GetCollection<User>("users");

            // Drop existing problematic email index if present
            try
            {
                users.Indexes.DropOne("Email_1");
            }
            catch (MongoCommandException)
            {
                // Index not found; ignore
            }

            // Create indexes for user collection
            users.Indexes.CreateMany(new[]{
                new CreateIndexModel<User>(Builders<User>.IndexKeys.Ascending(u => u.Role)),  // Index by role
                new CreateIndexModel<User>(Builders<User>.IndexKeys.Ascending(u => u.Email), new CreateIndexOptions{
                    Sparse = true,  // Skip documents without email
                    Background = true  // Build index in background
                }),
                new CreateIndexModel<User>(Builders<User>.IndexKeys.Ascending(u => u.Nic), new CreateIndexOptions{
                    Unique = true,  // Ensure NIC uniqueness
                    Sparse = true
                })
            });

            // Create indexes for station collection
            var stations = _ctx.Db.GetCollection<Station>("stations");
            stations.Indexes.CreateOne(new CreateIndexModel<Station>(Builders<Station>.IndexKeys.Ascending(s => s.Active)));  // Index by active status
            stations.Indexes.CreateOne(new CreateIndexModel<Station>(Builders<Station>.IndexKeys.Geo2DSphere("Location.GeoJson")));  // Geo index for location

            // Create indexes for booking collection
            var bookings = _ctx.Db.GetCollection<Booking>("bookings");
            var idx1 = Builders<Booking>.IndexKeys.Ascending(b => b.StationId).Ascending(b => b.StartTime).Ascending(b => b.Status);  // Station/time/status composite index
            var idx2 = Builders<Booking>.IndexKeys.Ascending(b => b.OwnerId).Ascending(b => b.StartTime);  // Owner/time index
            bookings.Indexes.CreateOne(new CreateIndexModel<Booking>(idx1));
            bookings.Indexes.CreateOne(new CreateIndexModel<Booking>(idx2));
        }
    }
}
