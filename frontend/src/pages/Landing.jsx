import React from 'react';
import { Link } from 'react-router-dom';
import Navbar from '../components/Navbar';
import { Compass, Sparkles, ShieldCheck, ArrowRight, SunMoon } from 'lucide-react';
import { useAuth } from '../context/AuthContext';

const Landing = () => {
  const { isAuthenticated } = useAuth();

  return (
    <div className="min-h-screen flex flex-col bg-surface-dark selection:bg-primary-500/30 selection:text-white">
      <Navbar />

      {/* Hero Section */}
      <main className="flex-grow flex flex-col items-center justify-center relative overflow-hidden px-6 py-20 text-center">
        {/* Glow Effects */}
        <div className="absolute top-1/4 left-1/2 -translate-x-1/2 -translate-y-1/2 w-96 h-96 bg-primary-600/10 rounded-full blur-3xl -z-10 animate-pulse-slow"></div>
        <div className="absolute bottom-1/4 left-1/3 w-80 h-80 bg-accent-600/10 rounded-full blur-3xl -z-10 animate-pulse-slow"></div>

        <div className="max-w-4xl mx-auto space-y-8 animate-slide-up">
          {/* Tagline */}
          <div className="inline-flex items-center gap-2 px-3 py-1.5 rounded-full border border-primary-500/20 bg-primary-500/5 text-primary-400 text-xs font-semibold tracking-wide">
            <Sparkles className="w-4 h-4 animate-spin-slow" />
            Empowered by AI Reasoning & Live Data
          </div>

          {/* Heading */}
          <h1 className="text-5xl md:text-7xl font-extrabold tracking-tight leading-tight text-white">
            Simulate Your Next Adventure.<br/>
            <span className="bg-gradient-to-r from-primary-400 via-purple-400 to-accent-400 bg-clip-text text-transparent">
              Compare "What-If" Scenarios.
            </span>
          </h1>

          {/* Subtitle */}
          <p className="text-lg md:text-xl text-slate-400 max-w-2xl mx-auto font-light leading-relaxed">
            Wondering what if you traveled in monsoon, or doubled your budget, or invited your family? Our simulator computes budget forecasts, live weather impacts, safety ratings, and custom itineraries side-by-side.
          </p>

          {/* CTA Buttons */}
          <div className="flex flex-col sm:flex-row items-center justify-center gap-4 pt-4">
            <Link 
              to={isAuthenticated ? "/builder" : "/register"} 
              className="btn-primary flex items-center gap-2 text-base px-8 py-3.5 group"
            >
              <span>Build Scenario Set</span>
              <ArrowRight className="w-5 h-5 group-hover:translate-x-1 transition-transform" />
            </Link>
            <Link 
              to="/login" 
              className="btn-secondary px-8 py-3.5"
            >
              Learn More
            </Link>
          </div>
        </div>

        {/* Feature Cards Grid */}
        <section className="max-w-7xl mx-auto grid grid-cols-1 md:grid-cols-3 gap-8 mt-24 px-6 text-left">
          {/* Card 1 */}
          <div className="card-hover">
            <div className="w-12 h-12 rounded-xl bg-primary-600/10 border border-primary-500/20 flex items-center justify-center mb-6">
              <Compass className="w-6 h-6 text-primary-400" />
            </div>
            <h3 className="text-lg font-bold text-slate-100 mb-2">Simulate Variations</h3>
            <p className="text-slate-400 text-sm font-light">
              Add natural-language "what-if" options. Test weather shifts, group dynamics, travel dates, or budget tiers.
            </p>
          </div>

          {/* Card 2 */}
          <div className="card-hover">
            <div className="w-12 h-12 rounded-xl bg-accent-600/10 border border-accent-500/20 flex items-center justify-center mb-6">
              <SunMoon className="w-6 h-6 text-accent-400" />
            </div>
            <h3 className="text-lg font-bold text-slate-100 mb-2">Explainable Decision Scoring</h3>
            <p className="text-slate-400 text-sm font-light">
              We calculate an explicit 0–100 score weighing budget fit, weather safety, crowd impact, and travel constraints.
            </p>
          </div>

          {/* Card 3 */}
          <div className="card-hover">
            <div className="w-12 h-12 rounded-xl bg-danger-600/10 border border-danger-500/20 flex items-center justify-center mb-6">
              <ShieldCheck className="w-6 h-6 text-danger-400" />
            </div>
            <h3 className="text-lg font-bold text-slate-100 mb-2">Safety Guardrails</h3>
            <p className="text-slate-400 text-sm font-light">
              Real calculations incorporating historical hazards, travel advisories, and local seasonal alerts.
            </p>
          </div>
        </section>
      </main>

      {/* Footer */}
      <footer className="py-8 border-t border-surface-border text-center text-xs text-slate-500 bg-surface-dark px-6">
        <p>© 2026 AI What-If Travel Simulator. All rights reserved.</p>
        <p className="mt-2 text-slate-600 max-w-lg mx-auto">
          *Estimates based on historical data and AI reasoning — always verify with official sources before travel.*
        </p>
      </footer>
    </div>
  );
};

export default Landing;
