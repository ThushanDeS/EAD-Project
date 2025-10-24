
using pulsecharge.Models;
using pulsecharge.Repositories;
using MongoDB.Bson;
using MongoDB.Driver;

namespace pulsecharge.Services
{
    public class AvailabilityService
    {
        private readonly BookingRepository _bookings;
        private readonly StationRepository _stations;
        public AvailabilityService(BookingRepository b, StationRepository s){ _bookings=b; _stations=s; }

        public async Task<object?> GetAvailabilityAsync(string stationId, DateTime date)
        {
            if (!ObjectId.TryParse(stationId, out var sid)) return null;
            var station = await _stations.FindAsync(sid);
            if (station == null || !station.Active) return null;

            var open = TimeOnly.Parse(station.Hours.Open);
            var close = TimeOnly.Parse(station.Hours.Close);

            var blocks = new List<object>();
            for (var t = open; t < close; t = t.AddHours(1))
            {
                var start = new DateTime(date.Year, date.Month, date.Day, t.Hour, 0, 0, DateTimeKind.Utc);
                var list = await _bookings.Collection
                    .Find(b => b.StationId == sid && b.StartTime == start && (b.Status=="pending" || b.Status=="approved"))
                    .ToListAsync();
                var takenSlots = list.Select(b => b.SlotNumber).ToHashSet();
                var freeSlots = Enumerable.Range(1, station.SlotCount).Where(s => !takenSlots.Contains(s)).ToArray();
                blocks.Add(new { start = t.ToString("HH:mm"), end = t.AddHours(1).ToString("HH:mm"),
                    free = freeSlots.Length, total = station.SlotCount, freeSlots });
            }
            return new { stationId = station.Id.ToString(), date = date.ToString("yyyy-MM-dd"), blocks };
        }
    }
}
