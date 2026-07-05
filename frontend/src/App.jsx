import React, { useState, useEffect, useCallback } from 'react'
import { searchProducts } from './api/search'
import { getFavorites, addFavorite, removeFavorite } from './api/auth'
import ProductCard from './components/ProductCard'
import Footer from './components/Footer'
import SaleTicker from './components/SaleTicker'
import GoogleSignInButton from './components/GoogleSignInButton'
import UserMenu from './components/UserMenu'
import { useAuth } from './auth/AuthContext'
import searchBg from './assets/search-bg.png'
import homeHero from './assets/home-hero.png'

const SUGGESTIONS = [
  'best TWS under 2000',
  'best phones under 30k',
  'best laptop under 50k',
  'best earphones under 1000',
  'best smartwatch under 5000',
]

export default function App() {
  const [query, setQuery] = useState('')
  const [loading, setLoading] = useState(false)
  const [result, setResult] = useState(null)
  const [error, setError] = useState(null)
  const [favoriteAsins, setFavoriteAsins] = useState(new Set())
  const { user, signOut } = useAuth()

  // Load favorite ASINs when the user signs in (or on mount if already signed in)
  useEffect(() => {
    if (!user) {
      setFavoriteAsins(new Set())
      return
    }
    getFavorites()
      .then(favs => setFavoriteAsins(new Set(favs.map(f => f.asin))))
      .catch(e => console.error('Failed to load favorites:', e))
  }, [user])

  const handleToggleFavorite = useCallback(async (product) => {
    if (!user) {
      alert('Sign in with Google to save favorites.')
      return
    }
    const isFav = favoriteAsins.has(product.asin)
    try {
      if (isFav) {
        await removeFavorite(product.asin)
        setFavoriteAsins(prev => {
          const next = new Set(prev)
          next.delete(product.asin)
          return next
        })
      } else {
        await addFavorite({
          asin: product.asin,
          productName: product.productName,
          affiliateUrl: product.affiliateUrl,
          imageUrl: product.imageUrl,
        })
        setFavoriteAsins(prev => new Set(prev).add(product.asin))
      }
    } catch (e) {
      console.error('Failed to update favorite:', e)
    }
  }, [user, favoriteAsins])

  const handleSearch = async (q) => {
    const sq = (q || query).trim()
    if (!sq) return
    if (!user) {
      setError('Sign in with Google to search for deals.')
      return
    }
    setQuery(sq)
    setLoading(true)
    setError(null)
    setResult(null)
    try {
      const data = await searchProducts(sq)
      setResult(data)
    } catch (e) {
      setError(e.message)
      if (e.message.includes('session has expired')) {
        signOut()
      }
    } finally {
      setLoading(false)
    }
  }

  return (
    <div style={{ minHeight: '100vh', background: 'var(--bg)' }}>
      {/* Header */}
      <header style={{
        borderBottom: '1px solid var(--border)',
        padding: '14px 24px',
        display: 'grid',
        gridTemplateColumns: '1fr auto 1fr',
        alignItems: 'center',
        gap: 12,
        position: 'sticky',
        top: 0,
        background: 'rgba(255,255,255,0.85)',
        backdropFilter: 'blur(12px)',
        zIndex: 100,
      }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
          <div style={{
            width: 34, height: 34, borderRadius: 9,
            background: 'var(--ink)',
            display: 'flex', alignItems: 'center', justifyContent: 'center',
            fontSize: 16, fontWeight: 800, color: '#fff',
          }}>D</div>
          <span style={{ fontWeight: 700, fontSize: 17, letterSpacing: '-0.3px', color: 'var(--ink)' }}>DealFinder</span>
        </div>
        <SaleTicker />
        <nav style={{ marginLeft: 'auto', display: 'flex', alignItems: 'center', gap: 20 }}>
          <a href="#about" style={{ fontSize: 13.5, fontWeight: 500, color: 'var(--text-muted)' }}>About</a>
          <a href="#contact" style={{ fontSize: 13.5, fontWeight: 500, color: 'var(--text-muted)' }}>Contact</a>
          <span style={{
            fontSize: 12, fontWeight: 500, color: 'var(--text-muted)',
            background: 'var(--bg3)', padding: '6px 12px', borderRadius: 20,
          }}>
            Powered by YouTube reviews
          </span>
          {user ? <UserMenu /> : <GoogleSignInButton />}
        </nav>
      </header>

      {/* Hero with overlaid glass search card */}
      {!result && !loading && (
        <div style={{
          position: 'relative',
          width: '100%',
          height: 'min(80vh, 720px)',
          minHeight: 480,
          overflow: 'hidden',
        }}>
          <img
            src={homeHero}
            alt="DealFinder — Redesigning for an Improved Queuing Experience"
            style={{
              position: 'absolute',
              inset: 0,
              width: '100%',
              height: '100%',
              objectFit: 'cover',
              objectPosition: 'center 25%',
            }}
          />
          {/* subtle bottom gradient so the glass card always has contrast to sit on */}
          <div style={{
            position: 'absolute',
            inset: 0,
            background: 'linear-gradient(180deg, rgba(0,0,0,0) 40%, rgba(0,0,0,0.55) 100%)',
          }} />

          {/* Glass search card */}
          <div style={{
            position: 'absolute',
            left: '50%',
            bottom: '7%',
            transform: 'translateX(-50%)',
            width: '92%',
            maxWidth: 640,
            background: 'rgba(20,22,28,0.42)',
            backdropFilter: 'blur(18px)',
            WebkitBackdropFilter: 'blur(18px)',
            border: '1px solid rgba(255,255,255,0.22)',
            borderRadius: 20,
            padding: '18px 18px 20px',
            boxShadow: '0 20px 50px rgba(0,0,0,0.35)',
          }}>
            {user ? (
              <>
                <div style={{
                  display: 'flex',
                  gap: 8,
                  background: 'rgba(255,255,255,0.95)',
                  borderRadius: 14,
                  padding: 6,
                  boxShadow: 'var(--shadow-md)',
                }}>
                  <span style={{ paddingLeft: 12, display: 'flex', alignItems: 'center', color: 'var(--text-dim)', fontSize: 17 }}>🔍</span>
                  <input
                    style={{
                      flex: 1,
                      background: 'transparent',
                      border: 'none',
                      outline: 'none',
                      color: 'var(--text)',
                      fontSize: 15,
                      padding: '10px 4px',
                      fontFamily: 'var(--font)',
                    }}
                    placeholder="best TWS under 2000, best phone under 30k..."
                    value={query}
                    onChange={e => setQuery(e.target.value)}
                    onKeyDown={e => e.key === 'Enter' && handleSearch()}
                  />
                  <button
                    onClick={() => handleSearch()}
                    style={{
                      background: 'var(--ink)',
                      color: '#fff',
                      fontWeight: 600,
                      fontSize: 14,
                      border: 'none',
                      borderRadius: 10,
                      padding: '10px 22px',
                      cursor: 'pointer',
                      whiteSpace: 'nowrap',
                      flexShrink: 0,
                    }}
                  >Search</button>
                </div>

                {/* Suggestions */}
                <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap', marginTop: 14, justifyContent: 'center' }}>
                  {SUGGESTIONS.map(s => (
                    <button key={s} onClick={() => handleSearch(s)} style={{
                      background: 'rgba(255,255,255,0.14)',
                      border: '1px solid rgba(255,255,255,0.3)',
                      color: '#fff',
                      fontSize: 12.5,
                      fontWeight: 500,
                      padding: '6px 14px',
                      borderRadius: 20,
                      cursor: 'pointer',
                      backdropFilter: 'blur(6px)',
                    }}>{s}</button>
                  ))}
                </div>
              </>
            ) : (
              <div style={{ textAlign: 'center', padding: '10px 4px' }}>
                <div style={{ color: '#fff', fontSize: 15, fontWeight: 600, marginBottom: 4 }}>
                  Sign in to search for deals
                </div>
                <div style={{ color: 'rgba(255,255,255,0.75)', fontSize: 13, marginBottom: 16 }}>
                  Your searches and favorites are saved to your account
                </div>
                <div style={{ display: 'flex', justifyContent: 'center' }}>
                  <GoogleSignInButton />
                </div>
              </div>
            )}
          </div>
        </div>
      )}

      {/* Compact search bar — shown once results/loading take over the hero */}
      {(result || loading) && (
        <div style={{ maxWidth: 640, margin: '24px auto 40px', padding: '0 20px' }}>
          <div style={{
            display: 'flex',
            gap: 8,
            background: 'var(--bg2)',
            border: '1px solid var(--border-strong)',
            borderRadius: 14,
            padding: 6,
            boxShadow: 'var(--shadow-md)',
          }}>
            <span style={{ paddingLeft: 12, display: 'flex', alignItems: 'center', color: 'var(--text-dim)', fontSize: 17 }}>🔍</span>
            <input
              style={{
                flex: 1,
                background: 'transparent',
                border: 'none',
                outline: 'none',
                color: 'var(--text)',
                fontSize: 15,
                padding: '10px 4px',
                fontFamily: 'var(--font)',
              }}
              placeholder="best TWS under 2000, best phone under 30k..."
              value={query}
              onChange={e => setQuery(e.target.value)}
              onKeyDown={e => e.key === 'Enter' && handleSearch()}
            />
            <button
              onClick={() => handleSearch()}
              style={{
                background: 'var(--ink)',
                color: '#fff',
                fontWeight: 600,
                fontSize: 14,
                border: 'none',
                borderRadius: 10,
                padding: '10px 22px',
                cursor: 'pointer',
                whiteSpace: 'nowrap',
                flexShrink: 0,
              }}
            >Search</button>
          </div>
        </div>
      )}

      {/* Spatial search overlay */}
      {loading && <SpatialSearchOverlay query={query} />}

      {/* Error */}
      {error && (
        <div style={{
          maxWidth: 480, margin: '40px auto', padding: '16px 20px',
          background: '#fef2f2', border: '1px solid #fecaca',
          borderRadius: 12, color: '#dc2626', fontSize: 14, textAlign: 'center',
        }}>⚠️ {error}</div>
      )}

      {/* Results */}
      {result && !loading && (
        <div style={{ maxWidth: 1200, margin: '0 auto', padding: '0 20px 60px' }}>
          <div style={{ marginBottom: 24, display: 'flex', alignItems: 'center', justifyContent: 'space-between', flexWrap: 'wrap', gap: 8 }}>
            <div>
              <h2 style={{ fontSize: 20, fontWeight: 700, letterSpacing: '-0.3px', color: 'var(--ink)' }}>
                Top picks for <span style={{ color: 'var(--accent)' }}>{result.query}</span>
              </h2>
              {result.totalProducts > 0 && (
                <p style={{ color: 'var(--text-muted)', fontSize: 13, marginTop: 4 }}>
                  Ranked by how many YouTube reviewers recommended them
                </p>
              )}
            </div>
            <button onClick={() => { setResult(null); setQuery('') }} style={{
              background: 'var(--bg2)', border: '1px solid var(--border-strong)',
              color: 'var(--text-muted)', fontSize: 13, fontWeight: 500, padding: '7px 16px',
              borderRadius: 10, cursor: 'pointer', boxShadow: 'var(--shadow-sm)',
            }}>← New search</button>
          </div>

          {result.totalProducts === 0 ? (
            <div style={{ textAlign: 'center', padding: '80px 20px' }}>
              <div style={{ fontSize: 40, marginBottom: 16 }}>🔍</div>
              <div style={{ fontSize: 18, fontWeight: 600, marginBottom: 8, color: 'var(--ink)' }}>No products found</div>
              <div style={{ color: 'var(--text-muted)', fontSize: 14 }}>
                Try a more specific query like "best phones under 30k" or "best TWS under 2000"
              </div>
            </div>
          ) : (
            <ProductGrid
              products={result.products}
              favoriteAsins={favoriteAsins}
              onToggleFavorite={handleToggleFavorite}
            />
          )}
        </div>
      )}

      <Footer />
    </div>
  )
}

function SpatialSearchOverlay({ query }) {
  const steps = [
    'Scanning trusted YouTube tech channels...',
    'Extracting affiliate links from descriptions...',
    'Ranking picks by reviewer consensus...',
    'Tagging your Amazon affiliate links...',
  ]
  const [stepIndex, setStepIndex] = React.useState(0)

  React.useEffect(() => {
    const id = setInterval(() => {
      setStepIndex(i => (i + 1) % steps.length)
    }, 1400)
    return () => clearInterval(id)
  }, [])

  return (
    <div style={{
      position: 'fixed',
      inset: 0,
      zIndex: 500,
      overflow: 'hidden',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      animation: 'fadeIn 0.35s ease',
      backgroundImage: `url(${searchBg})`,
      backgroundSize: 'cover',
      backgroundPosition: 'center',
    }}>
      {/* dark overlay for legibility — heavier toward center/right where the busy mockup is */}
      <div style={{
        position: 'absolute',
        inset: 0,
        background: 'radial-gradient(ellipse 700px 500px at 50% 50%, rgba(5,6,7,0.88) 0%, rgba(5,6,7,0.6) 45%, rgba(5,6,7,0.35) 100%)',
      }} />

      {/* scanning line */}
      <div style={{
        position: 'absolute',
        left: 0, right: 0,
        height: 2,
        background: 'linear-gradient(90deg, transparent, rgba(34,197,94,0.9), transparent)',
        boxShadow: '0 0 20px 4px rgba(34,197,94,0.5)',
        animation: 'scanY 2.6s ease-in-out infinite',
      }} />

      {/* content */}
      <div style={{ position: 'relative', textAlign: 'center', padding: '0 24px', maxWidth: 480 }}>
        <div style={{
          width: 64, height: 64, borderRadius: 18,
          background: 'rgba(34,197,94,0.12)',
          border: '1px solid rgba(34,197,94,0.4)',
          display: 'flex', alignItems: 'center', justifyContent: 'center',
          fontSize: 28,
          margin: '0 auto 26px',
          animation: 'pulseRing 1.8s ease-in-out infinite',
        }}>🔍</div>

        <div style={{
          fontSize: 13, fontWeight: 700, letterSpacing: '1.5px', textTransform: 'uppercase',
          color: '#4ade80', marginBottom: 10,
        }}>Deal<span style={{ color: '#fff' }}>finder</span></div>

        <div style={{ color: '#fff', fontSize: 19, fontWeight: 600, marginBottom: 14, letterSpacing: '-0.2px' }}>
          Searching for "{query}"
        </div>

        <div style={{ color: '#9ca3af', fontSize: 14, minHeight: 20, transition: 'opacity 0.3s' }} key={stepIndex}>
          {steps[stepIndex]}
        </div>

        <div style={{ display: 'flex', gap: 6, justifyContent: 'center', marginTop: 24 }}>
          {steps.map((_, i) => (
            <div key={i} style={{
              width: i === stepIndex ? 20 : 6, height: 6, borderRadius: 3,
              background: i === stepIndex ? '#4ade80' : 'rgba(255,255,255,0.15)',
              transition: 'all 0.3s ease',
            }} />
          ))}
        </div>
      </div>

      <style>{`
        @keyframes fadeIn { from { opacity: 0 } to { opacity: 1 } }
        @keyframes scanY { 0% { top: 15%; opacity: 0; } 10% { opacity: 1; } 90% { opacity: 1; } 100% { top: 85%; opacity: 0; } }
        @keyframes pulseRing { 0%, 100% { box-shadow: 0 0 0 0 rgba(34,197,94,0.4); } 50% { box-shadow: 0 0 0 10px rgba(34,197,94,0); } }
      `}</style>
    </div>
  )
}

function ProductGrid({ products, favoriteAsins, onToggleFavorite }) {
  if (!products || products.length === 0) return null
  const [featured, ...rest] = products

  return (
    <div>
      {/* Featured top pick */}
      <ProductCard
        product={featured}
        featured
        isFavorite={favoriteAsins?.has(featured.asin)}
        onToggleFavorite={() => onToggleFavorite(featured)}
      />

      {/* Rest in grid */}
      {rest.length > 0 && (
        <div style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(auto-fill, minmax(250px, 1fr))',
          gap: 18,
          marginTop: 18,
        }}>
          {rest.map((p, i) => (
            <ProductCard
              key={p.asin || i}
              product={p}
              isFavorite={favoriteAsins?.has(p.asin)}
              onToggleFavorite={() => onToggleFavorite(p)}
            />
          ))}
        </div>
      )}
    </div>
  )
}
