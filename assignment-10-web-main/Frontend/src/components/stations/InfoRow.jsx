const InfoRow = ({ icon: Icon, label, value, color }) => (
    <div className="flex items-center justify-between border-b border-gray-100 pb-2">
        <div className="flex items-center">
            <Icon className={`h-6 w-6 ${color} mr-3`} />
            <span className="text-gray-600 font-medium">{label}</span>
        </div>
        <span className={`text-gray-900 font-semibold ${color}`}>{value}</span>
    </div>
);

export default InfoRow;