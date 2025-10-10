import StatusBadge from '../common/StatusBadge';

const OperatorsTable = ({ users, onEdit, onToggleStatus, onDelete }) => {
    return (
        <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-gray-200">
                <thead className="bg-gray-50">
                    <tr>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Name</th>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Email</th>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">NIC</th>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Status</th>
                        <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">Actions</th>
                    </tr>
                </thead>
                <tbody className="bg-white divide-y divide-gray-200">
                    {users.map((user) => (
                        <tr key={user._id}>
                            <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">{user.name}</td>
                            <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{user.email}</td>
                            <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{user.nic}</td>
                            <td className="px-6 py-4 whitespace-nowrap text-sm">
                                <StatusBadge status={user.status} />
                            </td>
                            <td className="px-6 py-4 whitespace-nowrap text-sm text-right font-medium">
                                <div className="flex items-center justify-end space-x-4">
                                    <button onClick={() => onEdit(user)} className="text-primary-600 hover:text-primary-900">
                                        Edit
                                    </button>
                                    <button onClick={() => onToggleStatus(user)} className={user.status === 'active' ? 'text-yellow-600 hover:text-yellow-900' : 'text-green-600 hover:text-green-900'}>
                                        {user.status === 'active' ? 'Deactivate' : 'Reactivate'}
                                    </button>
                                    <button onClick={() => onDelete(user._id)} className="text-red-600 hover:text-red-900">
                                        Delete
                                    </button>
                                </div>
                            </td>
                        </tr>
                    ))}
                    {users.length === 0 && (
                        <tr>
                            <td colSpan="5" className="px-6 py-4 text-center text-gray-500">
                                No operators found
                            </td>
                        </tr>
                    )}
                </tbody>
            </table>
        </div>
    );
};

export default OperatorsTable;
