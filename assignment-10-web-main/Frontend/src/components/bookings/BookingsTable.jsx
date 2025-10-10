import { CheckIcon, XMarkIcon } from '@heroicons/react/24/outline';
import { useAuth } from '../../context/AuthContext';
import { formatToSriLankaTime } from '../../utils/timezone';
import StatusBadge from '../common/StatusBadge';

const BookingsTable = ({ bookings, onApprove, onCancel }) => {
    const { isBackoffice, isOperator } = useAuth();

    const formatDateTime = (dateString) => {
        return formatToSriLankaTime(dateString);
    };

    return (
        <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-gray-200">
                <thead className="bg-gray-50">
                    <tr>
                        <th scope="col" className="px-6 py-3 text-xs font-medium tracking-wider text-left text-gray-500 uppercase">EV NIC</th>
                        <th scope="col" className="px-6 py-3 text-xs font-medium tracking-wider text-left text-gray-500 uppercase">Station</th>
                        <th scope="col" className="px-6 py-3 text-xs font-medium tracking-wider text-left text-gray-500 uppercase">Start Time</th>
                        <th scope="col" className="px-6 py-3 text-xs font-medium tracking-wider text-left text-gray-500 uppercase">End Time</th>
                        <th scope="col" className="px-6 py-3 text-xs font-medium tracking-wider text-left text-gray-500 uppercase">Status</th>
                    </tr>
                </thead>
                <tbody className="bg-white divide-y divide-gray-200">
                    {bookings.map((booking) => (
                        <tr key={booking._id}>
                            <td className="px-6 py-4 text-sm font-medium text-gray-900 whitespace-nowrap">{booking.evNic}</td>
                            <td className="px-6 py-4 text-sm text-gray-500 whitespace-nowrap">{booking.stationName || 'Unknown Station'}</td>
                            <td className="px-6 py-4 text-sm text-gray-500 whitespace-nowrap">{formatDateTime(booking.startUtc)}</td>
                            <td className="px-6 py-4 text-sm text-gray-500 whitespace-nowrap">{formatDateTime(booking.endUtc)}</td>
                            <td className="px-6 py-4 text-sm text-gray-500 whitespace-nowrap">
                                <StatusBadge status={booking.status} />
                            </td>
                            <td className="px-6 py-4 text-sm font-medium text-right whitespace-nowrap">
                                <div className="flex items-center justify-end space-x-4">
                                    {booking.status === 'Pending' && (isBackoffice || isOperator) && (
                                        <>
                                            <button onClick={() => onApprove(booking._id, true)} className="text-green-600 hover:text-green-900" title="Approve">
                                                <CheckIcon className="w-5 h-5" />
                                            </button>
                                            <button onClick={() => onApprove(booking._id, false)} className="text-red-600 hover:text-red-900" title="Reject">
                                                <XMarkIcon className="w-5 h-5" />
                                            </button>
                                        </>
                                    )}
                                    {(booking.status === 'Pending' || booking.status === 'Approved') && (
                                        <button onClick={() => onCancel(booking._id)} className="font-medium text-yellow-600 hover:text-yellow-900">
                                            Cancel
                                        </button>
                                    )}
                                </div>
                            </td>
                        </tr>
                    ))}
                    {bookings.length === 0 && (
                        <tr>
                            <td colSpan="6" className="px-6 py-12 text-sm text-center text-gray-500">
                                No bookings found.
                            </td>
                        </tr>
                    )}
                </tbody>
            </table>
        </div>
    );
};

export default BookingsTable;
