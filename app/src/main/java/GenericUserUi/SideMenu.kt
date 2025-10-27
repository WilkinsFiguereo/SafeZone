import { useState } from 'react';
import { User, Home, Bell, MapPin, AlertCircle, Eye, Settings, Menu, X } from 'lucide-react';

export default function AlertaYaMenu() {
    const [isOpen, setIsOpen] = useState(true);

    const menuItems = [
        { icon: User, label: 'Perfil', id: 'perfil' },
        { icon: Home, label: 'Inicio', id: 'inicio' },
        { icon: Bell, label: 'Noticias', id: 'noticias' },
        { icon: MapPin, label: 'Reportes en tu zona', id: 'reportes' },
        { icon: AlertCircle, label: 'Alerta une porquemotica', id: 'alerta' },
        { icon: Eye, label: 'Mis alertas', id: 'mis-alertas' },
        { icon: Bell, label: 'Notificaciones', id: 'notificaciones' },
        { icon: Settings, label: 'Configuracion', id: 'config' },
    ];

    return (
    <div className="flex h-screen bg-gray-100">
    {/* Botón para móviles */}
    <button
    onClick={() => setIsOpen(!isOpen)}
    className="fixed top-4 left-4 z-50 md:hidden bg-green-500 text-white p-2 rounded-lg"
    >
    {isOpen ? <X size={24} /> : <Menu size={24} />}
    </button>

    {/* Menú Lateral */}
    <div
    className={`${
        isOpen ? 'w-64' : 'w-0'
    } transition-all duration-300 overflow-hidden bg-green-500 text-white flex flex-col`}
    >
    {/* Header del menú */}
    <div className="p-6 border-b border-green-600">
    <h2 className="text-2xl font-bold">AlertaYa</h2>
    </div>

    {/* Items del menú */}
    <nav className="flex-1 py-6">
    {menuItems.map((item) => {
        const Icon = item.icon;
        return (
        <button
        key={item.id}
        className="w-full flex items-center gap-4 px-6 py-4 hover:bg-green-600 transition-colors text-left group"
        >
        <Icon size={24} className="group-hover:scale-110 transition-transform" />
        <span className="text-lg font-medium">{item.label}</span>
        </button>
        );
    })}
    </nav>

    {/* Footer del menú */}
    <div className="p-6 border-t border-green-600 text-sm text-green-100">
    <p>Versión 1.0</p>
    </div>
    </div>

    {/* Contenido principal */}
    <div className="flex-1 p-6 md:p-12 overflow-auto">
    <div className="bg-white rounded-xl shadow-lg p-8">
    <h1 className="text-3xl font-bold text-green-600 mb-4">Contenido Principal</h1>
    <p className="text-gray-600">
    El menú está listo para ser integrado con tu contenido. Haz clic en los elementos del menú para navegar.
    </p>
    </div>
    </div>
    </div>
    );
}