import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { Compass, History, LogOut, LogIn, UserPlus } from 'lucide-react';

const Navbar = () => {
  const { isAuthenticated, logout, user } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/');
  };

  return (
    <nav className="bg-surface-card/80 backdrop-blur-md border-b border-surface-border sticky top-0 z-50 px-6 py-4">
      <div className="max-w-7xl mx-auto flex items-center justify-between">
        {/* Logo */}
        <Link to="/" className="flex items-center gap-2 group">
          <Compass className="w-8 h-8 text-primary-500 group-hover:rotate-45 transition-transform duration-300" />
          <span className="font-extrabold text-xl bg-gradient-to-r from-primary-400 to-accent-400 bg-clip-text text-transparent">
            What-If Travel
          </span>
        </Link>

        {/* Navigation Links */}
        <div className="flex items-center gap-6">
          {isAuthenticated ? (
            <>
              <Link to="/builder" className="flex items-center gap-2 text-slate-300 hover:text-white transition-colors py-1.5 px-3 rounded-lg hover:bg-surface-border/50">
                <Compass className="w-4 h-4" />
                <span>Scenario Builder</span>
              </Link>
              <Link to="/history" className="flex items-center gap-2 text-slate-300 hover:text-white transition-colors py-1.5 px-3 rounded-lg hover:bg-surface-border/50">
                <History className="w-4 h-4" />
                <span>My Trips</span>
              </Link>
              <div className="h-6 w-px bg-surface-border"></div>
              <div className="text-sm font-medium text-slate-400">
                Hi, <span className="text-slate-200 font-semibold">{user?.name || 'Explorer'}</span>
              </div>
              <button 
                onClick={handleLogout}
                className="flex items-center gap-2 text-danger-400 hover:text-danger-300 transition-colors py-1.5 px-3 rounded-lg hover:bg-danger-500/10"
              >
                <LogOut className="w-4 h-4" />
                <span>Logout</span>
              </button>
            </>
          ) : (
            <>
              <Link to="/login" className="flex items-center gap-2 text-slate-300 hover:text-white transition-colors">
                <LogIn className="w-4 h-4" />
                <span>Login</span>
              </Link>
              <Link to="/register" className="btn-primary flex items-center gap-2 !py-2 !px-4 text-sm">
                <UserPlus className="w-4 h-4" />
                <span>Get Started</span>
              </Link>
            </>
          )}
        </div>
      </div>
    </nav>
  );
};

export default Navbar;
