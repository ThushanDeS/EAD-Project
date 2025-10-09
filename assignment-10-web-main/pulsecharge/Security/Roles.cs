
namespace pulsecharge.Security
{
    public static class Roles
    {
        public const string Backoffice = "Backoffice";
        public const string StationOperator = "StationOperator";
        public const string EvOwner = "EvOwner";
    }

    public static class Policies
    {
        public const string BackofficeOnly = "BackofficeOnly";
        public const string OperatorOnly = "OperatorOnly";
        public const string OwnerOnly = "OwnerOnly";
    }
}
