/*
* File: StationDtos.cs
* Description: Contains Data Transfer Object (DTO) definitions used for creating, updating, 
*              and assigning operators to stations.
* Author: Thushan de Silva
*/ 

using pulsecharge.Models;

namespace pulsecharge.Dtos
{
    // DTO used for creating a new station
    public record CreateStationDto(string Name, string Type, int SlotCount, Location Location, Hours Hours, List<string>? OperatorIds);
    // DTO used for updating an existing station
    public record UpdateStationDto(string? Name, string? Type, int? SlotCount, Location? Location, Hours? Hours, List<string>? OperatorIds);
    // DTO used for assigning operators to a station
    public record AssignOperatorsDto(List<string> OperatorIds);
}