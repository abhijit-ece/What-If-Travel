import React, { createContext, useState, useEffect, useContext } from 'react';
import api from '../api/axios';
import { toast } from 'react-hot-toast';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  // Validate session on app initialization
  useEffect(() => {
    const initializeAuth = async () => {
      const token = localStorage.getItem('token');
      if (token) {
        try {
          // Fetch user details from /auth/me
          const response = await api.get('/auth/me');
          if (response.success && response.data) {
            setUser(response.data);
          } else {
            localStorage.removeItem('token');
          }
        } catch (error) {
          console.error('Session validation failed:', error);
          localStorage.removeItem('token');
        }
      }
      setLoading(false);
    };

    initializeAuth();
  }, []);

  // Login handler
  const login = async (email, password) => {
    try {
      setLoading(true);
      const response = await api.post('/auth/login', { email, password });
      
      if (response.success && response.data?.accessToken) {
        const token = response.data.accessToken;
        localStorage.setItem('token', token);
        
        // Fetch user data
        const profileResponse = await api.get('/auth/me');
        if (profileResponse.success && profileResponse.data) {
          setUser(profileResponse.data);
          toast.success(response.message || 'Login successful!');
          return profileResponse.data;
        }
      }
      throw new Error(response.message || 'Login failed');
    } catch (error) {
      toast.error(error.message || 'Invalid email or password');
      throw error;
    } finally {
      setLoading(false);
    }
  };

  // Sign up handler
  const signup = async (userData) => {
    try {
      setLoading(true);
      const response = await api.post('/auth/register', userData);
      if (response.success) {
        toast.success(response.message || 'Account created successfully! Please login.');
        return response.data;
      }
      throw new Error(response.message || 'Registration failed');
    } catch (error) {
      toast.error(error.message || 'Registration failed');
      throw error;
    } finally {
      setLoading(false);
    }
  };

  // Logout handler
  const logout = () => {
    localStorage.removeItem('token');
    setUser(null);
    toast.success('Logged out successfully');
  };

  const value = {
    user,
    loading,
    isAuthenticated: !!user,
    login,
    signup,
    logout,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
