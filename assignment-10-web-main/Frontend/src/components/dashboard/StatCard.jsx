const StatCard = ({ stat }) => {
    const { name, value, icon: Icon, color, bgColor, description } = stat;
    return (
        <div className={`card ${bgColor} p-5`}>
            <div className="flex items-start justify-between">
                <div className="flex-1">
                    <p className="text-sm font-medium text-gray-500 truncate">{name}</p>
                    <div className="mt-1 flex items-baseline">
                        <p className={`text-3xl font-semibold ${color}`}>{value}</p>
                        {description && (
                            <p className="ml-2 text-sm text-gray-500">{description}</p>
                        )}
                    </div>
                </div>
                <div className="flex-shrink-0 ml-4">
                    <Icon className={`h-8 w-8 ${color}`} aria-hidden="true" />
                </div>
            </div>
        </div>
    );
};

export default StatCard;
