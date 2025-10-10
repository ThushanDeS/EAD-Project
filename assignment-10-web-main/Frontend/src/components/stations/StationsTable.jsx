import { useAuth } from '../../context/AuthContext';

const StationsTable = ({ stations, operators, onEdit, onToggleStatus }) => {
    const { isBackoffice } = useAuth();
    const getOperatorName = (operatorId) => {
        if (!operatorId) return 'Unassigned';
        const operator = operators.find(op => op._id === operatorId);
        return operator ? operator.name : 'Unknown Operator';
    };

    return (
        <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-gray-200">
                <thead className="bg-gray-50">
                    <tr>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Name</th>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Operator</th>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Type</th>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Slots</th>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Status</th>
                        {isBackoffice && <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">Actions</th>}
                    </tr>
                </thead>
                <tbody className="bg-white divide-y divide-gray-200">
                    {stations.map((station) => (
                        <tr key={station._id}>
                            <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">{station.name}</td>
                            <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                                <span className={station.operatorId ? 'text-gray-900' : 'text-amber-600'}>
                                    {getOperatorName(station.operatorId)}
                                </span>
                            </td>
                            <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{station.type}</td>
                            <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{station.slotCount}</td>
                            <td className="px-6 py-4 whitespace-nowrap text-sm">
                                <span className={station.active ? 'text-green-600' : 'text-red-600'}>
                                    {station.active ? 'Active' : 'Inactive'}
                                </span>
                            </td>
                            {isBackoffice && (
                                <td className="px-6 py-4 whitespace-nowrap text-sm text-right font-medium">
                                    <div className="flex items-center justify-end space-x-4">
                                        <button onClick={() => onEdit(station)} className="text-primary-600 hover:text-primary-900">
                                            Edit
                                        </button>
                                        <button onClick={() => onToggleStatus(station)} className={station.active ? 'text-yellow-600 hover:text-yellow-900' : 'text-green-600 hover:text-green-900'}>
                                            {station.active ? 'Deactivate' : 'Reactivate'}
                                        </button>
                                    </div>
                                </td>
                            )}
                        </tr>
                    ))}
                    {stations.length === 0 && (
                        <tr>
                            <td colSpan={isBackoffice ? 6 : 5} className="px-6 py-4 text-center text-gray-500">
                                No stations found
                            </td>
                        </tr>
                    )}
                </tbody>
            </table>
        </div>
    );
};

export default StationsTable;
