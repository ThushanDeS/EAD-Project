/*
 * File Name   : Station.cs
 * Description : Defines the data model for an EV charging station, including details such as 
 *               station name, location, type, available slots, operating hours, and assigned 
 *               operators. Also includes helper classes for location and operating hours.
 * Author      : Thushan de Silva
*/

using MongoDB.Bson;
using MongoDB.Bson.Serialization.Attributes;
using MongoDB.Driver.GeoJsonObjectModel;

namespace pulsecharge.Models
{
    public class Station
    {
        [BsonId] public ObjectId Id { get; set; }
        public string Name { get; set; } = default!;
        public Location Location { get; set; } = default!;
        public string Type { get; set; } = "AC";
        public int SlotCount { get; set; }
        public bool Active { get; set; } = true;
        public List<ObjectId> OperatorIds { get; set; } = new();
        public Hours Hours { get; set; } = new() { Open = "09:00", Close = "21:00" };
        public DateTime CreatedAt { get; set; } = DateTime.UtcNow;
        public DateTime UpdatedAt { get; set; } = DateTime.UtcNow;
    }

    public class Hours
    {
        public string Open { get; set; } = "09:00";
        public string Close { get; set; } = "21:00";
    }

    public class Location
    {
        public double Lat { get; set; }
        public double Lng { get; set; }
        public string? Address { get; set; }
        public GeoJsonPoint<GeoJson2DGeographicCoordinates> GeoJson
            => new GeoJsonPoint<GeoJson2DGeographicCoordinates>(new GeoJson2DGeographicCoordinates(Lng, Lat));
    }
}
