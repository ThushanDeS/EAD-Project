using pulsecharge.Models;

namespace pulsecharge.Dtos
{
    public record CreateStationDto(string Name, string Type, int SlotCount, Location Location, Hours Hours, List<string>? OperatorIds);
    public record UpdateStationDto(string? Name, string? Type, int? SlotCount, Location? Location, Hours? Hours, List<string>? OperatorIds);
    public record AssignOperatorsDto(List<string> OperatorIds);
}