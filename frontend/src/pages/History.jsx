import React, { useEffect, useState } from 'react';
import Navbar from '../components/Navbar';
import { History as HistoryIcon, ArrowRight, Calendar, Compass, Loader, Trash2 } from 'lucide-react';
import { Link } from 'react-router-dom';
import api from '../api/axios';
import { toast } from 'react-hot-toast';

const History = () => {
  const [sessions, setSessions] = useState([]);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    fetchHistory();
  }, []);

  const fetchHistory = async () => {
    try {
      setIsLoading(true);
      const response = await api.get('/scenarios/history');
      if (response.success && response.data) {
        setSessions(response.data);
      } else {
        throw new Error(response.message || 'Failed to load history');
      }
    } catch (error) {
      toast.error(error.message || 'Error loading simulation history');
    } finally {
      setIsLoading(false);
    }
  };

  const getBaseTripDetails = (baseTripJson) => {
    try {
      const parsed = JSON.parse(baseTripJson);
      return parsed;
    } catch (e) {
      return null;
    }
  };

  return (
    <div className="min-h-screen bg-surface-dark flex flex-col selection:bg-primary-500/30 selection:text-white">
      <Navbar />

      <main className="flex-grow max-w-7xl mx-auto px-6 py-12 w-full space-y-8 animate-slide-up">
        {/* Header */}
        <div className="border-b border-surface-border pb-6">
          <h1 className="text-3xl font-extrabold text-white flex items-center gap-2">
            <HistoryIcon className="w-8 h-8 text-primary-500" />
            Comparison History
          </h1>
          <p className="text-slate-400 text-sm mt-1 font-light">
            Review and open your past "what-if" simulations.
          </p>
        </div>

        {/* History List */}
        {isLoading ? (
          <div className="flex flex-col items-center justify-center py-20">
            <Loader className="w-10 h-10 border-primary-500 animate-spin text-primary-500" />
            <p className="text-slate-400 mt-4 text-sm font-light">Loading History Log...</p>
          </div>
        ) : sessions.length === 0 ? (
          <div className="card text-center py-20 space-y-4">
            <Compass className="w-12 h-12 text-slate-500 mx-auto animate-pulse" />
            <h3 className="font-bold text-lg text-slate-200">No Simulations Found</h3>
            <p className="text-slate-400 text-sm font-light max-w-md mx-auto">
              You haven't run any "what-if" travel simulations yet. Go back to the builder to start your first comparison set.
            </p>
            <Link to="/builder" className="btn-primary inline-block">
              Create New Set
            </Link>
          </div>
        ) : (
          <div className="space-y-4">
            {sessions.map(session => {
              const baseTrip = getBaseTripDetails(session.baseTripJson);
              const dateStr = session.createdAt ? session.createdAt.substring(0, 10) : '';

              return (
                <div key={session.id} className="card bg-surface-card hover:border-primary-500/30 transition-all flex flex-col md:flex-row md:items-center justify-between gap-6 p-6">
                  <div className="flex items-center gap-4">
                    <div className="w-12 h-12 rounded-xl bg-primary-600/10 border border-primary-500/20 flex items-center justify-center flex-shrink-0">
                      <Compass className="w-6 h-6 text-primary-400" />
                    </div>
                    <div className="space-y-1">
                      <h3 className="font-bold text-slate-200 text-lg">
                        {baseTrip?.destination || 'Unknown Destination'}
                      </h3>
                      <div className="flex flex-wrap items-center gap-x-4 gap-y-1 text-xs text-slate-400 font-light">
                        <span className="flex items-center gap-1">
                          <Calendar className="w-3.5 h-3.5" />
                          Simulated on: {dateStr}
                        </span>
                        <span>•</span>
                        <span>Stated Budget: ₹{baseTrip?.budget?.toLocaleString()}</span>
                        <span>•</span>
                        <span>{session.scenarios?.length || 0} Scenarios</span>
                      </div>
                    </div>
                  </div>

                  <Link 
                    to={`/compare/${session.id}`} 
                    className="btn-secondary !py-2.5 !px-4 text-xs flex items-center gap-2 self-start md:self-auto hover:bg-surface-border text-center"
                  >
                    <span>Open Simulation</span>
                    <ArrowRight className="w-4 h-4" />
                  </Link>
                </div>
              );
            })}
          </div>
        )}

        {/* Disclaimer */}
        <p className="text-center text-[10px] text-slate-600 max-w-lg mx-auto pt-6">
          *Estimates based on historical data and AI reasoning — always verify with official sources before travel.*
        </p>
      </main>
    </div>
  );
};

export default History;
