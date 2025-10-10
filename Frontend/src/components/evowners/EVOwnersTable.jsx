import StatusBadge from '../common/StatusBadge';

const EVOwnersTable = ({ owners, onEdit, onToggleStatus }) => {
    return (
        <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-gray-200">
                <thead className="bg-gray-50">
                    <tr>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">NIC</th>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Name</th>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Email</th>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Phone</th>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Status</th>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Actions</th>
                    </tr>
                </thead>
                <tbody className="bg-white divide-y divide-gray-200">
                    {owners.map((owner) => (
                        <tr key={owner.id}>
                            <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">{owner.nic}</td>
                            <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{owner.name}</td>
                            <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{owner.email}</td>
                            <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{owner.phone}</td>
                            <td className="px-6 py-4 whitespace-nowrap">
                                <StatusBadge status={owner.status} />
                            </td>
                            <td className="px-6 py-4 whitespace-nowrap text-sm font-medium space-x-4">
                                <button onClick={() => onEdit(owner)} className="text-indigo-600 hover:text-indigo-900">Edit</button>
                                <button onClick={() => onToggleStatus(owner)} className={owner.status === 'active' ? 'text-red-600 hover:text-red-900' : 'text-green-600 hover:text-green-900'}>
                                    {owner.status === 'active' ? 'Deactivate' : 'Activate'}
                                </button>
                            </td>
                        </tr>
                    ))}
                    {owners.length === 0 && (
                        <tr>
                            <td colSpan="6" className="px-6 py-4 text-center text-gray-500">
                                No owners found
                            </td>
                        </tr>
                    )}
                </tbody>
            </table>
        </div>
    );
};

export default EVOwnersTable;
