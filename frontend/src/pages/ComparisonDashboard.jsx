import React, { useEffect, useState } from 'react';
import Navbar from '../components/Navbar';
import { useParams, Link } from 'react-router-dom';
import api from '../api/axios';
import { toast } from 'react-hot-toast';
import { 
  ChevronLeft, Download, Award, AlertTriangle, ShieldCheck, 
  HelpCircle, Info, Calendar, Train, Sun, Layers, Loader, X
} from 'lucide-react';
import { 
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, 
  ResponsiveContainer, Cell
} from 'recharts';

const ComparisonDashboard = () => {
  const { sessionId } = useParams();
  const [session, setSession] = useState(null);
  const [isLoading, setIsLoading] = useState(true);
  const [computeIndex, setComputeIndex] = useState(-1);
  const [computeTotal, setComputeTotal] = useState(0);
  const [selectedScenario, setSelectedScenario] = useState(null); // For breakdown modal
  const [exporting, setExporting] = useState(false);

  useEffect(() => {
    fetchSessionDetails();
  }, [sessionId]);

  const fetchSessionDetails = async () => {
    try {
      setIsLoading(true);
      const response = await api.get(`/scenarios/session/${sessionId}`);
      if (response.success && response.data) {
        setSession(response.data);
        
        // Find uncomputed scenarios
        const uncomputed = response.data.scenarios.filter(s => !s.hasResult);
        if (uncomputed.length > 0) {
          setComputeTotal(uncomputed.length);
          setComputeIndex(0);
          triggerSequentialComputation(uncomputed, 0, response.data);
        } else {
          setIsLoading(false);
        }
      } else {
        throw new Error(response.message || 'Failed to fetch session details');
      }
    } catch (error) {
      toast.error(error.message || 'Error loading comparison dashboard');
      setIsLoading(false);
    }
  };

  const triggerSequentialComputation = async (uncomputedList, index, currentSession) => {
    if (index >= uncomputedList.length) {
      // Finished all computations! Reload details.
      const response = await api.get(`/scenarios/session/${sessionId}`);
      if (response.success && response.data) {
        setSession(response.data);
      }
      setComputeIndex(-1);
      setIsLoading(false);
      toast.success('Simulation completed successfully!');
      return;
    }

    setComputeIndex(index);
    const scenario = uncomputedList[index];

    try {
      const response = await api.post(`/scenarios/${scenario.id}/compute`);
      if (response.success && response.data) {
        // Update scenario in state locally
        currentSession.scenarios = currentSession.scenarios.map(s => 
          s.id === scenario.id ? response.data : s
        );
        setSession({ ...currentSession });
      } else {
        throw new Error(response.message || 'Computation failed');
      }
    } catch (error) {
      toast.error(`Computation failed for "${scenario.label}": ${error.message}`);
    }

    // Move to next scenario
    triggerSequentialComputation(uncomputedList, index + 1, currentSession);
  };

  const handleExportPdf = async () => {
    try {
      setExporting(true);
      toast.loading('Generating comparison report...', { id: 'pdf-toast' });
      
      const response = await api.get(`/scenarios/session/${sessionId}/export`, {
        responseType: 'blob'
      });

      // Download file stream
      const blob = new Blob([response], { type: 'application/pdf' });
      const link = document.createElement('a');
      link.href = window.URL.createObjectURL(blob);
      link.download = `comparison_report_${sessionId}.pdf`;
      link.click();
      
      toast.success('Report downloaded!', { id: 'pdf-toast' });
    } catch (error) {
      toast.error('Could not export PDF report', { id: 'pdf-toast' });
    } finally {
      setExporting(false);
    }
  };

  const openBreakdownModal = (scenario) => {
    if (!scenario.hasResult) return;
    try {
      const breakdown = JSON.parse(scenario.decisionBreakdownJson);
      setSelectedScenario({ ...scenario, breakdown });
    } catch (e) {
      toast.error('Could not parse score breakdown.');
    }
  };

  const closeBreakdownModal = () => {
    setSelectedScenario(null);
  };

  if (isLoading) {
    return (
      <div className="min-h-screen bg-surface-dark flex flex-col items-center justify-center">
        <div className="w-16 h-16 border-4 border-primary-500 border-t-transparent rounded-full animate-spin"></div>
        {computeIndex >= 0 ? (
          <div className="text-center mt-6 space-y-2">
            <p className="text-slate-200 font-semibold text-lg animate-pulse">
              Computing Scenario {computeIndex + 1} of {computeTotal}
            </p>
            <p className="text-slate-400 text-sm font-light max-w-sm">
              Fetching live weather updates, calculating Haversine travelconvenience, and generating AI itineraries...
            </p>
          </div>
        ) : (
          <p className="text-slate-400 mt-4 font-light">Loading Comparison Details...</p>
        )}
      </div>
    );
  }

  // Find recommended scenario (winner = highest decisionScore)
  const computedScenarios = session?.scenarios.filter(s => s.hasResult) || [];
  const recommendedScenario = computedScenarios.reduce((prev, current) => 
    (prev.decisionScore > current.decisionScore) ? prev : current
  , { decisionScore: -1 });

  // Prep Recharts data
  const chartData = computedScenarios.map(s => ({
    name: s.isBaseScenario ? "Base Plan" : s.label.length > 15 ? s.label.substring(0, 15) + "..." : s.label,
    Score: s.decisionScore,
    fullLabel: s.label
  }));

  // Recharts colors
  const COLORS = ['#6172f9', '#10b981', '#ef4444', '#f59e0b', '#ec4899'];

  return (
    <div className="min-h-screen bg-surface-dark flex flex-col selection:bg-primary-500/30 selection:text-white">
      <Navbar />

      <main className="flex-grow max-w-7xl mx-auto px-6 py-12 w-full space-y-8 animate-slide-up">
        {/* Top Actions */}
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 border-b border-surface-border pb-6">
          <div className="flex items-center gap-4">
            <Link to="/builder" className="btn-secondary !py-2 !px-3 flex items-center gap-1.5 text-xs">
              <ChevronLeft className="w-4 h-4" />
              Back to Builder
            </Link>
            <div className="h-6 w-px bg-surface-border"></div>
            <div>
              <h1 className="text-2xl font-extrabold text-white flex items-center gap-2">
                Scenario Comparison Dashboard
              </h1>
              <p className="text-slate-400 text-xs">Session #{sessionId}</p>
            </div>
          </div>

          <button
            onClick={handleExportPdf}
            className="btn-primary !py-2.5 !px-4 text-sm flex items-center gap-2"
            disabled={exporting}
          >
            <Download className="w-4 h-4" />
            <span>{exporting ? 'Exporting...' : 'Export PDF Report'}</span>
          </button>
        </div>

        {/* 🏆 Recommended Scenario Winner Banner */}
        {recommendedScenario.decisionScore > 0 && (
          <div className="p-6 rounded-2xl bg-gradient-to-r from-primary-900/30 via-primary-800/10 to-surface-card border border-primary-500/30 flex flex-col md:flex-row items-start md:items-center justify-between gap-6 shadow-xl relative overflow-hidden">
            <div className="absolute top-0 right-0 w-40 h-40 bg-primary-500/5 rounded-full blur-2xl -z-10"></div>
            <div className="flex items-start gap-4">
              <div className="w-12 h-12 bg-primary-600/20 border border-primary-500/30 rounded-xl flex items-center justify-center text-primary-400 flex-shrink-0">
                <Award className="w-6 h-6" />
              </div>
              <div className="space-y-1">
                <div className="flex items-center gap-2">
                  <span className="text-xs font-bold text-primary-400 uppercase tracking-wide">AI Recommendation</span>
                  <span className="badge bg-accent-500/20 text-accent-400 border border-accent-500/30">Winner</span>
                </div>
                <h3 className="font-bold text-xl text-slate-100">{recommendedScenario.label}</h3>
                <p className="text-slate-400 text-sm font-light max-w-4xl leading-relaxed">
                  {recommendedScenario.aiExplanation}
                </p>
              </div>
            </div>
            <div className="text-right flex-shrink-0 self-end md:self-auto">
              <div className="text-[10px] uppercase font-bold text-slate-500 tracking-wider">Winning Score</div>
              <div className="text-3xl font-extrabold text-accent-400">{recommendedScenario.decisionScore}<span className="text-sm font-normal text-slate-500">/100</span></div>
            </div>
          </div>
        )}

        {/* Dash Data Content Grid */}
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* Main Cards list */}
          <div className="lg:col-span-2 space-y-6">
            <h2 className="text-lg font-bold text-slate-200 flex items-center gap-2">
              <Layers className="w-5 h-5 text-slate-400" />
              Simulated Scenarios
            </h2>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              {computedScenarios.map((scenario, index) => (
                <div 
                  key={scenario.id} 
                  className={`card relative flex flex-col justify-between transition-all duration-200 border-surface-border ${
                    scenario.id === recommendedScenario.id ? 'border-primary-500/50 bg-primary-950/5' : 'hover:border-slate-700'
                  }`}
                >
                  {scenario.isBaseScenario ? (
                    <span className="absolute top-4 right-4 badge bg-primary-500/10 text-primary-400 border border-primary-500/20">Base Plan</span>
                  ) : (
                    <span className="absolute top-4 right-4 badge bg-amber-500/10 text-amber-400 border border-amber-500/20">What-If #{index}</span>
                  )}

                  <div className="space-y-4">
                    <div>
                      <h3 className="font-bold text-slate-100 text-base max-w-[75%] leading-snug">{scenario.label}</h3>
                      <div className="flex items-center gap-2 text-xs text-slate-500 mt-1 font-light">
                        <Calendar className="w-3.5 h-3.5" />
                        <span>{scenario.startDate} • {scenario.travelMode.toUpperCase()}</span>
                      </div>
                    </div>

                    <div className="space-y-2 pt-2 border-t border-surface-border/50">
                      <div className="flex justify-between items-center text-xs">
                        <span className="text-slate-400 font-light">Decision Score</span>
                        <span className="font-bold text-slate-200">{scenario.decisionScore}/100</span>
                      </div>
                      <div className="flex justify-between items-center text-xs">
                        <span className="text-slate-400 font-light">Weather Forecast</span>
                        <span className="font-medium text-slate-300">{scenario.weatherSummary}</span>
                      </div>
                      <div className="flex justify-between items-center text-xs">
                        <span className="text-slate-400 font-light">Convenience Time</span>
                        <span className="font-medium text-slate-300">{scenario.travelTimeEstimate}</span>
                      </div>
                      <div className="flex justify-between items-center text-xs">
                        <span className="text-slate-400 font-light">Stated Budget</span>
                        <span className="font-medium text-slate-300">₹{scenario.budget.toLocaleString()}</span>
                      </div>
                      <div className="flex justify-between items-center text-xs">
                        <span className="text-slate-400 font-light">Hidden Extras</span>
                        <span className="font-medium text-danger-400">+₹{scenario.hiddenExpenses.toLocaleString()}</span>
                      </div>
                    </div>
                  </div>

                  <div className="flex items-center gap-3 mt-6 pt-4 border-t border-surface-border/50">
                    <button
                      onClick={() => openBreakdownModal(scenario)}
                      className="btn-secondary !py-2 text-xs flex-grow hover:bg-surface-border text-center"
                    >
                      Score Breakdown
                    </button>
                  </div>
                </div>
              ))}
            </div>
          </div>

          {/* Right Charts Sidebar */}
          <div className="space-y-6">
            <div className="card space-y-4">
              <h3 className="font-bold text-slate-200 text-sm flex items-center gap-1.5">
                <Sun className="w-4 h-4 text-primary-400" />
                Score Comparison
              </h3>
              <div className="h-64 w-full">
                <ResponsiveContainer width="100%" height="100%">
                  <BarChart data={chartData} margin={{ top: 20, right: 10, left: -20, bottom: 5 }}>
                    <CartesianGrid strokeDasharray="3 3" stroke="#1e293b" />
                    <XAxis dataKey="name" stroke="#94a3b8" fontSize={10} tickLine={false} />
                    <YAxis domain={[0, 100]} stroke="#94a3b8" fontSize={10} tickLine={false} />
                    <Tooltip 
                      contentStyle={{ background: '#1e293b', border: '1px solid #334155', borderRadius: '0.75rem' }} 
                      labelStyle={{ color: '#94a3b8', fontWeight: 'bold' }}
                      itemStyle={{ color: '#fff' }}
                    />
                    <Bar dataKey="Score" radius={[4, 4, 0, 0]}>
                      {chartData.map((entry, index) => (
                        <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                      ))}
                    </Bar>
                  </BarChart>
                </ResponsiveContainer>
              </div>
            </div>

            {/* Safety & Monsoon alert notifications */}
            <div className="card border-amber-500/20 bg-amber-500/5 flex items-start gap-3 p-4">
              <AlertTriangle className="w-5 h-5 text-amber-500 flex-shrink-0 mt-0.5" />
              <div className="space-y-1">
                <h4 className="text-xs font-bold text-amber-500 uppercase tracking-wide">Monsoon & Weather Guardrails</h4>
                <p className="text-[10px] text-slate-400 font-light leading-relaxed">
                  Trip scenarios hitting heavy monsoon cycles (June - Sept in tropical/mountain areas) have safety deductions applied to protect against flash floods, landslides, and transport closures.
                </p>
              </div>
            </div>

            {/* Disclaimer */}
            <div className="p-4 rounded-xl border border-surface-border bg-surface-card/30 text-[10px] text-slate-500 leading-relaxed">
              <strong>Disclaimer:</strong> Estimates based on historical data and AI reasoning — always verify with official sources before travel.
            </div>
          </div>
        </div>
      </main>

      {/* 📊 Score Breakdown Modal */}
      {selectedScenario && (
        <div className="fixed inset-0 bg-slate-950/80 backdrop-blur-sm z-50 flex items-center justify-center p-6">
          <div className="w-full max-w-xl bg-surface-card border border-surface-border rounded-2xl shadow-2xl p-6 relative animate-fade-in">
            {/* Close Button */}
            <button 
              onClick={closeBreakdownModal} 
              className="absolute top-4 right-4 p-2 text-slate-400 hover:text-white rounded-lg hover:bg-surface-border/50"
            >
              <X className="w-5 h-5" />
            </button>

            <div className="mb-6">
              <span className="text-[10px] font-bold text-primary-400 uppercase tracking-wider block">Decision Score Breakdown</span>
              <h2 className="text-xl font-bold text-slate-100 mt-1">{selectedScenario.label}</h2>
              <p className="text-xs text-slate-400 font-light mt-0.5">Final Score: <strong className="text-accent-400 font-semibold">{selectedScenario.decisionScore}</strong></p>
            </div>

            {/* Subscores bar graphs */}
            <div className="space-y-4 max-h-[300px] overflow-y-auto pr-1">
              {[
                { label: 'Budget Fit (25%)', score: selectedScenario.breakdown.budgetFit, color: 'bg-primary-500' },
                { label: 'Weather Suitability (20%)', score: selectedScenario.breakdown.weatherSuitability, color: 'bg-accent-500' },
                { label: 'Crowd Score (15%)', score: selectedScenario.breakdown.crowdLevelScore, color: 'bg-yellow-500' },
                { label: 'Safety Index (15%)', score: selectedScenario.breakdown.safety, color: 'bg-danger-500' },
                { label: 'Travel Convenience (10%)', score: selectedScenario.breakdown.convenience, color: 'bg-indigo-500' },
                { label: 'Hidden Cost Index (10%)', score: selectedScenario.breakdown.hiddenExpenseRiskScore, color: 'bg-purple-500' },
                { label: 'Preference Match (5%)', score: selectedScenario.breakdown.preferenceMatch, color: 'bg-teal-500' },
              ].map((sub, i) => (
                <div key={i} className="space-y-1">
                  <div className="flex justify-between text-xs font-medium">
                    <span className="text-slate-300">{sub.label}</span>
                    <span className="text-slate-200">{sub.score} <span className="text-slate-500">/ 100</span></span>
                  </div>
                  <div className="w-full h-2.5 bg-surface-dark rounded-full overflow-hidden border border-surface-border/50">
                    <div 
                      className={`h-full ${sub.color} rounded-full transition-all duration-500`}
                      style={{ width: `${sub.score}%` }}
                    ></div>
                  </div>
                </div>
              ))}
            </div>

            <div className="mt-8 pt-4 border-t border-surface-border/80 flex items-center justify-between text-[10px] text-slate-500">
              <span className="flex items-center gap-1">
                <Info className="w-3.5 h-3.5" />
                Explainable Scoring Logic Active
              </span>
              <span>Always verify before travel.</span>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default ComparisonDashboard;
