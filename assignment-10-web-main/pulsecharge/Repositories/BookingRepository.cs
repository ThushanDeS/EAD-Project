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

        /// <summary>
        /// Exposes the underlying collection for advanced operations or aggregation pipelines.
        /// Use sparingly from services; prefer repository helpers for common queries.
        /// </summary>
        public IMongoCollection<Booking> Collection => _bookingSet;

        /// <summary>
        /// Insert a new booking document.
        /// </summary>
        public Task CreateAsync(Booking entity) => _bookingSet.InsertOneAsync(entity);

        /// <summary>
        /// Replace an existing booking document. Matches on Id.
        /// </summary>
        public Task ReplaceAsync(Booking entity) => _bookingSet.ReplaceOneAsync(x => x.Id == entity.Id, entity);

        /// <summary>
        /// Find a booking by ObjectId. Returns null if not found.
        /// Awaiting the underlying driver call to satisfy nullable reference typing.
        /// </summary>
        public async Task<Booking?> FindAsync(ObjectId id)
        {
            return await _bookingSet.Find(b => b.Id == id).FirstOrDefaultAsync();
        }

        /// <summary>
        /// Returns all bookings. Suitable for backoffice listing where pagination is applied by callers.
        /// </summary>
        public Task<List<Booking>> GetAllAsync() => _bookingSet.Find(new BsonDocument()).ToListAsync();

        /// <summary>
        /// Count bookings that start at the exact UTC hour block for a station and are in active states.
        /// Used to enforce limits per time block.
        /// </summary>
        public Task<long> CountInBlock(ObjectId stationId, DateTime start) =>
            _bookingSet.CountDocumentsAsync(b => b.StationId == stationId && b.StartTime == start && (b.Status == "pending" || b.Status == "approved"));

        /// <summary>
        /// Check if a given slot is already occupied for the given start block (pending or approved).
        /// </summary>
        public Task<bool> ExistsSlot(ObjectId stationId, DateTime start, int slotNumber) =>
            _bookingSet.Find(b => b.StationId == stationId && b.StartTime == start && b.SlotNumber == slotNumber && (b.Status == "pending" || b.Status == "approved")).AnyAsync();

        /// <summary>
        /// Detects whether a new booking (or an updated booking) would overlap with existing active bookings
        /// for the same station and slot. The overlap logic checks for any existing booking where start < newEnd
        /// AND end > newStart (i.e. intervals intersect). Optionally excludes a booking id when checking updates.
        /// </summary>
        public Task<bool> HasOverlappingBooking(ObjectId stationId, int slotNumber, DateTime startTime, DateTime endTime, ObjectId? excludeBookingId = null)
        {
            // Base filters: same station, same slot, and only consider pending/approved bookings
            var filter = Builders<Booking>.Filter.And(
                Builders<Booking>.Filter.Eq(b => b.StationId, stationId),
                Builders<Booking>.Filter.Eq(b => b.SlotNumber, slotNumber),
                Builders<Booking>.Filter.In(b => b.Status, new[] { "pending", "approved" }),
                // Overlap condition: existing.Start < newEnd AND existing.End > newStart
                Builders<Booking>.Filter.And(
                    Builders<Booking>.Filter.Lt(b => b.StartTime, endTime),
                    Builders<Booking>.Filter.Gt(b => b.EndTime, startTime)
                )
            );

            // When updating an existing booking, exclude it from the overlap query
            if (excludeBookingId.HasValue)
            {
                filter = Builders<Booking>.Filter.And(
                    filter,
                    Builders<Booking>.Filter.Ne(b => b.Id, excludeBookingId.Value)
                );
            }

            return _bookingSet.Find(filter).AnyAsync();
        }

        /// <summary>
        /// Return all bookings for a station.
        /// </summary>
        public Task<List<Booking>> ForStationAsync(ObjectId stationId) =>
            _bookingSet.Find(b => b.StationId == stationId).ToListAsync();

        /// <summary>
        /// Return all bookings for an owner/user.
        /// </summary>
        public Task<List<Booking>> ForOwnerAsync(ObjectId ownerId) =>
            _bookingSet.Find(b => b.OwnerId == ownerId).ToListAsync();
    }
}
