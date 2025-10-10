const StatCard = ({ stat }) => {
    const { name, value, icon: Icon, color, bgColor } = stat;
    return (
        <div
            className={`flex flex-col items-center justify-center rounded-2xl ${bgColor} transition transform hover:scale-[1.03]`}
        >
            <div className={`p-5 rounded-full bg-white mb-5 ${color}`}>
                <Icon className="h-12 w-12" />
            </div>
            <p className="text-gray-600 text-lg font-medium">{name}</p>
            <p className={`mt-2 text-4xl font-extrabold ${color}`}>{value}</p>
        </div>
    );
};

export default StatCard;