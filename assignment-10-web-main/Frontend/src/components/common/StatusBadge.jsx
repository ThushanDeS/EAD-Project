const StatusBadge = ({ status }) => {
    const getStatusStyles = (status) => {
        const lowerCaseStatus = status?.toLowerCase() || '';
        if (lowerCaseStatus.startsWith('deactivated')) return 'bg-red-100 text-red-800';
        if (lowerCaseStatus === 'pending') return 'bg-yellow-100 text-yellow-800';
        if (lowerCaseStatus === 'approved') return 'bg-green-100 text-green-800';
        if (lowerCaseStatus === 'completed') return 'bg-blue-100 text-blue-800';
        if (lowerCaseStatus === 'in progress') return 'bg-indigo-100 text-indigo-800';
        if (lowerCaseStatus === 'rejected') return 'bg-red-100 text-red-800';
        if (lowerCaseStatus === 'cancelled') return 'bg-gray-100 text-gray-800';
        if (lowerCaseStatus === 'active') return 'bg-green-100 text-green-800';
        return 'bg-gray-100 text-gray-800';
    };

    const style = getStatusStyles(status);
    const text = status ? status.replace(/_/g, ' ') : 'Unknown';

    return (
        <span className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full capitalize ${style}`}>
            {text}
        </span>
    );
};

export default StatusBadge;
