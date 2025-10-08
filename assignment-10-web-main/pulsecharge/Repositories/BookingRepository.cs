using pulsecharge.Models;
using pulsecharge.Mongo;
using MongoDB.Bson;
using MongoDB.Driver;

namespace pulsecharge.Repositories
{
    public class BookingRepository
    {
        private readonly IMongoCollection<Booking> _bookingSet;
        public BookingRepository(MongoContext dbContext) { _bookingSet = dbContext.Db.GetCollection<Booking>("bookings"); }

        public IMongoCollection<Booking> Collection => _bookingSet;

        public Task CreateAsync(Booking entity) => _bookingSet.InsertOneAsync(entity);
        public Task ReplaceAsync(Booking entity) => _bookingSet.ReplaceOneAsync(x => x.Id == entity.Id, entity);
        public Task<Booking?> FindAsync(ObjectId id) => _bookingSet.Find(b => b.Id == id).FirstOrDefaultAsync();

        public Task<List<Booking>> GetAllAsync() => _bookingSet.Find(new BsonDocument()).ToListAsync();

        public Task<long> CountInBlock(ObjectId stationId, DateTime start) =>
            _bookingSet.CountDocumentsAsync(b => b.StationId == stationId && b.StartTime == start && (b.Status == "pending" || b.Status == "approved"));

        public Task<bool> ExistsSlot(ObjectId stationId, DateTime start, int slotNumber) =>
            _bookingSet.Find(b => b.StationId == stationId && b.StartTime == start && b.SlotNumber == slotNumber && (b.Status == "pending" || b.Status == "approved")).AnyAsync();

        public Task<bool> HasOverlappingBooking(ObjectId stationId, int slotNumber, DateTime startTime, DateTime endTime, ObjectId? excludeBookingId = null)
        {
            var filter = Builders<Booking>.Filter.And(
                Builders<Booking>.Filter.Eq(b => b.StationId, stationId),
                Builders<Booking>.Filter.Eq(b => b.SlotNumber, slotNumber),
                Builders<Booking>.Filter.In(b => b.Status, new[] { "pending", "approved" }),
                Builders<Booking>.Filter.Or(
                    Builders<Booking>.Filter.And(
                        Builders<Booking>.Filter.Lt(b => b.StartTime, endTime),
                        Builders<Booking>.Filter.Gt(b => b.EndTime, startTime)
                    )
                )
            );

            if (excludeBookingId.HasValue)
            {
                filter = Builders<Booking>.Filter.And(
                    filter,
                    Builders<Booking>.Filter.Ne(b => b.Id, excludeBookingId.Value)
                );
            }

            return _bookingSet.Find(filter).AnyAsync();
        }

        public Task<List<Booking>> ForStationAsync(ObjectId stationId) =>
            _bookingSet.Find(b => b.StationId == stationId).ToListAsync();

        public Task<List<Booking>> ForOwnerAsync(ObjectId ownerId) =>
            _bookingSet.Find(b => b.OwnerId == ownerId).ToListAsync();
    }
}
