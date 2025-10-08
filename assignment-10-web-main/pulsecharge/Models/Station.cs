using MongoDB.Bson;
using MongoDB.Bson.Serialization.Attributes;
using MongoDB.Driver.GeoJsonObjectModel;

namespace pulsecharge.Models
{
    public class Station
    {
        [BsonId]
        public ObjectId Id { get; set; }

        public string Name { get; set; } = default!;

        public Location Location { get; set; } = default!;

        public string Type { get; set; } = "AC";

        public int SlotCount { get; set; }

        public DateTime CreatedAt { get; set; } = DateTime.UtcNow;
    }

    public class Location
    {
        public double Lat { get; set; }

        public double Lng { get; set; }

        public string? Address { get; set; }

        public GeoJsonPoint<GeoJson2DGeographicCoordinates> GeoJson
            => new GeoJsonPoint<GeoJson2DGeographicCoordinates>(
                new GeoJson2DGeographicCoordinates(Lng, Lat));
    }
}
