// BookingRepository.cs
// Data access layer for Booking documents in MongoDB. This class encapsulates common queries
// and filters so the service layer can remain focused on business logic.
using pulsecharge.Models;
using pulsecharge.Mongo;
using MongoDB.Bson;
using MongoDB.Driver;

namespace pulsecharge.Repositories
{
    public class BookingRepository
    {
        private readonly IMongoCollection<Booking> _bookingSet;

        // Collection is obtained from the shared MongoContext
        public BookingRepository(MongoContext dbContext) { _bookingSet = dbContext.Db.GetCollection<Booking>("bookings"); }

        public IMongoCollection<Booking> Collection => _bookingSet;

        // Basic CRUD helpers
        public Task CreateAsync(Booking entity) => _bookingSet.InsertOneAsync(entity);
        public Task ReplaceAsync(Booking entity) => _bookingSet.ReplaceOneAsync(x => x.Id == entity.Id, entity);
    // Return nullable Booking (null when not found)
    public async Task<Booking?> FindAsync(ObjectId id) => await _bookingSet.Find(b => b.Id == id).FirstOrDefaultAsync();

        public Task<List<Booking>> GetAllAsync() => _bookingSet.Find(new BsonDocument()).ToListAsync();

        // Count bookings in an exact start-time block for a station (used for capacity checks)
        public Task<long> CountInBlock(ObjectId stationId, DateTime start) =>
            _bookingSet.CountDocumentsAsync(b => b.StationId == stationId && b.StartTime == start && (b.Status == "pending" || b.Status == "approved"));

        // Check if a specific slot (by slot number) is already taken at a given start time
        public Task<bool> ExistsSlot(ObjectId stationId, DateTime start, int slotNumber) =>
            _bookingSet.Find(b => b.StationId == stationId && b.StartTime == start && b.SlotNumber == slotNumber && (b.Status == "pending" || b.Status == "approved")).AnyAsync();

        // Detect overlapping bookings: any booking that starts before endTime and ends after startTime
        // only considers pending and approved bookings, and can exclude the current booking when updating
        public Task<bool> HasOverlappingBooking(ObjectId stationId, int slotNumber, DateTime startTime, DateTime endTime, ObjectId? excludeBookingId = null)
        {
            var filter = Builders<Booking>.Filter.And(
                Builders<Booking>.Filter.Eq(b => b.StationId, stationId),
                Builders<Booking>.Filter.Eq(b => b.SlotNumber, slotNumber),
                Builders<Booking>.Filter.In(b => b.Status, new[] { "pending", "approved" }),
                // overlap condition: booking.Start < endTime AND booking.End > startTime
                Builders<Booking>.Filter.And(
                    Builders<Booking>.Filter.Lt(b => b.StartTime, endTime),
                    Builders<Booking>.Filter.Gt(b => b.EndTime, startTime)
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
