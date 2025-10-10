import { Fragment } from 'react';
import { NavLink } from 'react-router-dom';
import { Menu, Transition } from '@headlessui/react';
import { useAuth } from '../../context/AuthContext';
import {
    HomeIcon,
    UsersIcon,
    BuildingOfficeIcon,
    CalendarIcon,
    PlusCircleIcon,
    DocumentTextIcon,
    QrCodeIcon,
    ChevronDownIcon,
    UserIcon,
} from '@heroicons/react/24/outline';
import { BoltIcon } from '@heroicons/react/24/solid';

const TopNav = () => {
    const { user, logout } = useAuth();

    const navigation = [
        { name: 'Dashboard', href: '/dashboard', icon: HomeIcon, roles: ['Backoffice'] },
        { name: 'Dashboard', href: '/operator-dashboard', icon: HomeIcon, roles: ['StationOperator'] },
        { name: 'Operators', href: '/operators', icon: UsersIcon, roles: ['Backoffice'] },
        { name: 'All Stations', href: '/stations', icon: BuildingOfficeIcon, roles: ['Backoffice'] },
        { name: 'My Stations', href: '/my-station', icon: BuildingOfficeIcon, roles: ['StationOperator'] },
        { name: 'All Bookings', href: '/bookings', icon: CalendarIcon, roles: ['Backoffice'] },
        { name: 'Create Booking', href: '/create-booking', icon: PlusCircleIcon, roles: ['Backoffice'] },
        { name: 'Station Bookings', href: '/station-bookings', icon: CalendarIcon, roles: ['StationOperator'] },
        { name: 'EV Owners', href: '/ev-owners', icon: DocumentTextIcon, roles: ['Backoffice'] },
    ];

    const filteredNavigation = navigation.filter((item) =>
        item.roles.includes(user?.role)
    );

    return (
        <header className="bg-white border-b border-slate-200 sticky top-0 z-30">
            <div className="mx-auto max-w-screen-xl px-4 sm:px-6 lg:px-8">
                <div className="flex h-16 items-center justify-between">
                    <div className="flex items-center gap-8">
                        <div className="flex items-center gap-2 flex-shrink-0">
                            <div className="bg-slate-900 p-2 rounded-lg">
                                <BoltIcon className="h-5 w-5 text-white" />
                            </div>
                            <span className="text-base font-bold text-slate-800 hidden sm:block">
                                Pulse Charge
                            </span>
                        </div>
                        <nav className="hidden md:flex items-center gap-1">
                            {filteredNavigation.map((item) => (
                                <NavLink
                                    key={`${item.name}-${item.href}`}
                                    to={item.href}
                                    end
                                    className={({ isActive }) =>
                                        `inline-flex items-center gap-2 rounded-md px-3 py-2 text-sm font-medium transition-colors ${isActive
                                            ? 'bg-slate-100 text-slate-900'
                                            : 'text-slate-500 hover:bg-slate-100 hover:text-slate-700'
                                        }`
                                    }
                                >
                                    <item.icon className="h-4 w-4" />
                                    {item.name}
                                </NavLink>
                            ))}
                        </nav>
                    </div>

                    <div className="flex items-center">
                        <Menu as="div" className="relative">
                            <Menu.Button className="flex items-center gap-2 text-sm font-medium text-slate-600 hover:text-slate-900 focus:outline-none">
                                <UserIcon className="h-6 w-6 p-1 bg-slate-100 rounded-full text-slate-500" />
                                <span className="hidden sm:block">{user?.name}</span>
                                <ChevronDownIcon className="h-4 w-4 text-slate-400 hidden sm:block" />
                            </Menu.Button>
                            <Transition
                                as={Fragment}
                                enter="transition ease-out duration-100"
                                enterFrom="transform opacity-0 scale-95"
                                enterTo="transform opacity-100 scale-100"
                                leave="transition ease-in duration-75"
                                leaveFrom="transform opacity-100 scale-100"
                                leaveTo="transform opacity-0 scale-95"
                            >
                                <Menu.Items className="absolute right-0 mt-2 w-48 origin-top-right bg-white rounded-md border border-slate-200 focus:outline-none z-50">
                                    <div className="p-1">
                                        <Menu.Item>
                                            {({ active }) => (
                                                <button
                                                    onClick={logout}
                                                    className={`${active ? 'bg-slate-100' : ''
                                                        } flex w-full items-center rounded-md px-3 py-2 text-sm text-slate-700`}
                                                >
                                                    Logout
                                                </button>
                                            )}
                                        </Menu.Item>
                                    </div>
                                </Menu.Items>
                            </Transition>
                        </Menu>
                    </div>
                </div>
            </div>
        </header>
    );
};

export default TopNav;