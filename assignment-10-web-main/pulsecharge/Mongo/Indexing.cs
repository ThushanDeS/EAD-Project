
using pulsecharge.Models;
using MongoDB.Driver;

namespace pulsecharge.Mongo
{
    public class Indexing
    {
        private readonly MongoContext _ctx;
        public Indexing(MongoContext ctx){ _ctx = ctx; }

        public void EnsureAll()
        {
            var users = _ctx.Db.GetCollection<User>("users");
            
            // Drop existing problematic email index if it exists
            try
            {
                users.Indexes.DropOne("Email_1");
            }
            catch (MongoCommandException)
            {
                // Index doesn't exist, which is fine
            }
            
            users.Indexes.CreateMany(new[]{
                new CreateIndexModel<User>(Builders<User>.IndexKeys.Ascending(u => u.Role)),
                new CreateIndexModel<User>(Builders<User>.IndexKeys.Ascending(u => u.Email), new CreateIndexOptions{ 
                    Sparse = true,
                    Background = true
                }),
                new CreateIndexModel<User>(Builders<User>.IndexKeys.Ascending(u => u.Nic), new CreateIndexOptions{ Unique=true, Sparse=true })
            });

            var stations = _ctx.Db.GetCollection<Station>("stations");
            stations.Indexes.CreateOne(new CreateIndexModel<Station>(Builders<Station>.IndexKeys.Ascending(s => s.Active)));
            stations.Indexes.CreateOne(new CreateIndexModel<Station>(Builders<Station>.IndexKeys.Geo2DSphere("Location.GeoJson")));

            var bookings = _ctx.Db.GetCollection<Booking>("bookings");
            var idx1 = Builders<Booking>.IndexKeys.Ascending(b => b.StationId).Ascending(b => b.StartTime).Ascending(b => b.Status);
            var idx2 = Builders<Booking>.IndexKeys.Ascending(b => b.OwnerId).Ascending(b => b.StartTime);
            bookings.Indexes.CreateOne(new CreateIndexModel<Booking>(idx1));
            bookings.Indexes.CreateOne(new CreateIndexModel<Booking>(idx2));
        }
    }
}
