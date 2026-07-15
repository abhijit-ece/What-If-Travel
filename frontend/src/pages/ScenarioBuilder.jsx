import React, { useState } from 'react';
import Navbar from '../components/Navbar';
import { Compass, Plus, Trash2, Sparkles, AlertCircle } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import api from '../api/axios';
import { toast } from 'react-hot-toast';

const ScenarioBuilder = () => {
  const [destination, setDestination] = useState('');
  const [startDate, setStartDate] = useState('');
  const [budget, setBudget] = useState('');
  const [groupType, setGroupType] = useState('solo');
  const [travelMode, setTravelMode] = useState('flight');
  const [whatIfs, setWhatIfs] = useState(['']);
  const [isLoading, setIsLoading] = useState(false);
  const navigate = useNavigate();

  const handleAddWhatIf = () => {
    setWhatIfs([...whatIfs, '']);
  };

  const handleRemoveWhatIf = (index) => {
    const updated = whatIfs.filter((_, i) => i !== index);
    setWhatIfs(updated.length === 0 ? [''] : updated);
  };

  const handleWhatIfChange = (index, value) => {
    const updated = [...whatIfs];
    updated[index] = value;
    setWhatIfs(updated);
  };

  const applyTemplate = (index, templateText) => {
    const updated = [...whatIfs];
    updated[index] = templateText;
    setWhatIfs(updated);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!destination || !startDate || !budget) {
      toast.error('Please enter all base trip specifications.');
      return;
    }

    // Filter empty what ifs
    const cleanWhatIfs = whatIfs.filter(w => w.trim() !== '');
    if (cleanWhatIfs.length === 0) {
      toast.error('Please add at least one What-If variation to compare.');
      return;
    }

    try {
      setIsLoading(true);
      const response = await api.post('/scenarios/session', {
        destination,
        startDate,
        budget: parseFloat(budget),
        groupType,
        travelMode,
        whatIfs: cleanWhatIfs
      });

      if (response.success && response.data) {
        toast.success(response.message || 'Session created! Launching simulator...');
        navigate(`/compare/${response.data.id}`);
      } else {
        throw new Error(response.message || 'Failed to create session');
      }
    } catch (error) {
      toast.error(error.message || 'Error creating simulation session');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-surface-dark flex flex-col selection:bg-primary-500/30 selection:text-white">
      <Navbar />

      <main className="flex-grow max-w-7xl mx-auto px-6 py-12 w-full space-y-8 animate-slide-up">
        {/* Header */}
        <div className="border-b border-surface-border pb-6">
          <h1 className="text-3xl font-extrabold text-white flex items-center gap-2">
            <Compass className="w-8 h-8 text-primary-500" />
            AI "What-If" Travel Simulator
          </h1>
          <p className="text-slate-400 text-sm mt-1 font-light">
            Model alternative travel scenarios instantly. Describe what-if variables and evaluate budget, weather, and safety tradeoffs.
          </p>
        </div>

        {/* Builder Layout */}
        <form onSubmit={handleSubmit} className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* Main Specifications Form */}
          <div className="lg:col-span-2 space-y-6">
            <div className="card space-y-6">
              <h2 className="text-lg font-bold text-slate-200 pb-2 border-b border-surface-border">
                1. Stated Base Trip Specifications
              </h2>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                {/* Destination */}
                <div className="space-y-2">
                  <label className="text-xs font-semibold text-slate-300 uppercase tracking-wider block">Destination City</label>
                  <input
                    type="text"
                    value={destination}
                    onChange={(e) => setDestination(e.target.value)}
                    placeholder="e.g. Goa, Paris, Tokyo"
                    className="input-field"
                    required
                    disabled={isLoading}
                  />
                </div>

                {/* Date & Budget Grid */}
                <div className="grid grid-cols-2 gap-4">
                  <div className="space-y-2">
                    <label className="text-xs font-semibold text-slate-300 uppercase tracking-wider block">Start Date</label>
                    <input
                      type="date"
                      value={startDate}
                      onChange={(e) => setStartDate(e.target.value)}
                      className="input-field"
                      required
                      disabled={isLoading}
                    />
                  </div>
                  <div className="space-y-2">
                    <label className="text-xs font-semibold text-slate-300 uppercase tracking-wider block">Stated Budget (INR)</label>
                    <input
                      type="number"
                      value={budget}
                      onChange={(e) => setBudget(e.target.value)}
                      placeholder="₹ Amount"
                      className="input-field"
                      required
                      disabled={isLoading}
                    />
                  </div>
                </div>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                {/* Group Type */}
                <div className="space-y-2">
                  <label className="text-xs font-semibold text-slate-300 uppercase tracking-wider block">Group Composition</label>
                  <select
                    value={groupType}
                    onChange={(e) => setGroupType(e.target.value)}
                    className="input-field cursor-pointer"
                    disabled={isLoading}
                  >
                    <option value="solo">Solo Traveler</option>
                    <option value="couple">Couple</option>
                    <option value="family">Family (Kids/Seniors)</option>
                    <option value="friends">Friends Group</option>
                  </select>
                </div>

                {/* Travel Mode */}
                <div className="space-y-2">
                  <label className="text-xs font-semibold text-slate-300 uppercase tracking-wider block">Default Travel Mode</label>
                  <select
                    value={travelMode}
                    onChange={(e) => setTravelMode(e.target.value)}
                    className="input-field cursor-pointer"
                    disabled={isLoading}
                  >
                    <option value="flight">Flight</option>
                    <option value="train">Train</option>
                    <option value="car">Road Trip (Car)</option>
                  </select>
                </div>
              </div>
            </div>

            {/* What If Variations section */}
            <div className="card space-y-6">
              <div className="flex items-center justify-between pb-2 border-b border-surface-border">
                <h2 className="text-lg font-bold text-slate-200">
                  2. Add "What-If" Variations
                </h2>
                <button
                  type="button"
                  onClick={handleAddWhatIf}
                  className="btn-secondary !py-1.5 !px-3 text-xs flex items-center gap-1 hover:bg-surface-border"
                  disabled={isLoading}
                >
                  <Plus className="w-3.5 h-3.5" />
                  Add Variation
                </button>
              </div>

              <div className="space-y-6">
                {whatIfs.map((whatIf, idx) => (
                  <div key={idx} className="space-y-3 p-4 rounded-xl border border-surface-border/60 bg-surface-dark/20">
                    <div className="flex items-center gap-4">
                      <span className="text-xs font-bold text-primary-400 uppercase tracking-wide">
                        What-If #{idx + 1}
                      </span>
                      <input
                        type="text"
                        value={whatIf}
                        onChange={(e) => handleWhatIfChange(idx, e.target.value)}
                        placeholder="e.g. What if I visit during the rainy season?"
                        className="input-field !py-2.5 !text-sm flex-grow"
                        required
                        disabled={isLoading}
                      />
                      {whatIfs.length > 1 && (
                        <button
                          type="button"
                          onClick={() => handleRemoveWhatIf(idx)}
                          className="p-2.5 text-slate-500 hover:text-danger-400 rounded-lg hover:bg-danger-500/10 transition-colors"
                          disabled={isLoading}
                        >
                          <Trash2 className="w-5 h-5" />
                        </button>
                      )}
                    </div>

                    {/* Pre-configured Templates */}
                    <div className="flex flex-wrap gap-2 items-center">
                      <span className="text-[10px] font-semibold text-slate-500 uppercase">Suggestions:</span>
                      <button
                        type="button"
                        onClick={() => applyTemplate(idx, "What if rainy season?")}
                        className="text-xs px-2.5 py-1 rounded bg-surface-card hover:bg-surface-border border border-surface-border/50 text-slate-300"
                        disabled={isLoading}
                      >
                        Monsoon
                      </button>
                      <button
                        type="button"
                        onClick={() => applyTemplate(idx, "What if winter season?")}
                        className="text-xs px-2.5 py-1 rounded bg-surface-card hover:bg-surface-border border border-surface-border/50 text-slate-300"
                        disabled={isLoading}
                      >
                        Winter Blizzard
                      </button>
                      <button
                        type="button"
                        onClick={() => applyTemplate(idx, "What if I travel by train?")}
                        className="text-xs px-2.5 py-1 rounded bg-surface-card hover:bg-surface-border border border-surface-border/50 text-slate-300"
                        disabled={isLoading}
                      >
                        Train Travel
                      </button>
                      <button
                        type="button"
                        onClick={() => applyTemplate(idx, `What if increase budget to ${parseFloat(budget || 0) * 1.5}?`)}
                        className="text-xs px-2.5 py-1 rounded bg-surface-card hover:bg-surface-border border border-surface-border/50 text-slate-300"
                        disabled={isLoading || !budget}
                      >
                        +50% Budget
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            </div>

            {/* Submission CTA */}
            <button
              type="submit"
              className="btn-primary w-full py-4 flex items-center justify-center gap-2"
              disabled={isLoading}
            >
              <Sparkles className="w-5 h-5 text-accent-400" />
              <span>{isLoading ? 'Creating Session...' : 'Generate and Compare Scenarios'}</span>
            </button>
          </div>

          {/* Quick Info & Guide */}
          <div className="space-y-6">
            <div className="card space-y-4">
              <h3 className="font-bold text-slate-200 text-sm flex items-center gap-2">
                <AlertCircle className="w-4 h-4 text-primary-400" />
                How the Simulator Works
              </h3>
              <div className="text-xs text-slate-400 space-y-3 pl-1 font-light leading-relaxed">
                <p>
                  <strong>Stated Base Specs</strong> establish your primary timeline, transportation mode, and target budget threshold.
                </p>
                <p>
                  <strong>What-If Variations</strong> allow modifying any travel parameter. The simulator intelligently parses natural language statements to construct distinct simulation branches.
                </p>
                <p>
                  <strong>Compute Pipeline</strong> retrieves real-time weather details, runs haversine navigation, and queries Gemini LLMs for safety notes, hidden surcharges, and customized itineraries.
                </p>
              </div>
            </div>

            {/* Disclaimer */}
            <div className="p-4 rounded-xl border border-surface-border bg-surface-card/30 text-[10px] text-slate-500 leading-relaxed">
              <strong>Disclaimer:</strong> Estimates based on historical data and AI reasoning — always verify with official sources before travel.
            </div>
          </div>
        </form>
      </main>
    </div>
  );
};

export default ScenarioBuilder;
