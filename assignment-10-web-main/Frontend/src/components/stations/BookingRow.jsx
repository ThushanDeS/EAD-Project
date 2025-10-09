import { CheckIcon, XMarkIcon } from "@heroicons/react/24/outline";
import { formatToSriLankaTime } from "../../utils/timezone";
import StatusBadge from "../common/StatusBadgeOperator";

const BookingRow = ({ booking, error, onApprove, onReject, onComplete }) => {
    const { _id, slotNumber, startTime, endTime, status } = booking;

    return (
        <tr className="border-b border-gray-100 hover:bg-gray-50 transition-colors">
            <td className="px-6 py-3 font-semibold text-gray-900">{slotNumber || "-"}</td>
            <td className="px-6 py-3">
                {startTime && endTime ? (
                    `${formatToSriLankaTime(startTime)} – ${formatToSriLankaTime(endTime)}`
                ) : (
                    "-"
                )}
            </td>
            <td className="px-6 py-3">
                <StatusBadge status={status || "Pending"} />
            </td>
            <td className="px-6 py-3 text-right">
                <div className="flex justify-end gap-2">
                    {status === "pending" && (
                        <>
                            <button
                                onClick={() => onApprove(_id)}
                                className="flex items-center gap-1 px-3 py-1 text-green-700 hover:bg-green-50 rounded-lg transition-colors"
                            >
                                <CheckIcon className="w-4 h-4" /> Approve
                            </button>
                            <button
                                onClick={() => onReject(_id)}
                                className="flex items-center gap-1 px-3 py-1 text-red-700 hover:bg-red-50 rounded-lg transition-colors"
                            >
                                <XMarkIcon className="w-4 h-4" /> Reject
                            </button>
                        </>
                    )}
                    {status === "approved" && (
                        <button
                            onClick={() => onComplete(_id)}
                            className="px-3 py-1 text-blue-700 hover:bg-blue-50 rounded-lg transition-colors"
                        >
                            Complete
                        </button>
                    )}
                </div>
                {error && <p className="text-xs text-red-600 mt-1">{error}</p>}
            </td>
        </tr>
    );
};

export default BookingRow;