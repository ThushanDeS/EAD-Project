const StatusBadgeOperator = ({ status }) => {
    const getStatusStyles = (s) => {
        if (!s) return "bg-gray-100 text-gray-700";
        const lowerCaseStatus = s.toLowerCase();

        const styles = {
            pending: "bg-yellow-100 text-yellow-800",
            approved: "bg-green-100 text-green-800",
            completed: "bg-blue-100 text-blue-800",
            cancelled: "bg-red-100 text-red-800",
            rejected: "bg-red-100 text-red-800",
            active: "bg-green-100 text-green-800",
        };

        if (lowerCaseStatus.startsWith("deactivated")) return "bg-red-100 text-red-800";
        return styles[lowerCaseStatus] || "bg-gray-100 text-gray-700";
    };

    const style = getStatusStyles(status);
    const text = status ? status.replace(/_/g, ' ') : 'Unknown';

    return (
        <span className={`px-2.5 py-1 rounded-full text-xs font-semibold capitalize ${style}`}>
            {text}
        </span>
    );
};

export default StatusBadgeOperator;