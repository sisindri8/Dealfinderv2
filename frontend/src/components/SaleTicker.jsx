import React, { useEffect, useState } from 'react'

// Maintain known/confirmed Amazon India sale windows here.
// Update this list as new sales are announced — the component
// figures out "live now" vs "starts in X days" automatically.
const SALE_EVENTS = [
  {
    name: 'Prime Day Sale',
    start: '2026-07-04T00:00:00+05:30',
    end: '2026-07-06T23:59:59+05:30',
  },
  {
    name: 'Great Freedom Festival',
    start: '2026-08-01T00:00:00+05:30',
    end: '2026-08-03T23:59:59+05:30',
  },
  {
    name: 'Great Indian Festival',
    start: '2026-09-23T00:00:00+05:30',
    end: '2026-09-30T23:59:59+05:30',
  },
]

const AMAZON_URL = 'https://www.amazon.in/deals'

function formatDuration(ms) {
  const totalMinutes = Math.floor(ms / 60000)
  const days = Math.floor(totalMinutes / 1440)
  const hours = Math.floor((totalMinutes % 1440) / 60)
  const minutes = totalMinutes % 60
  if (days > 0) return `${days}d ${hours}h`
  if (hours > 0) return `${hours}h ${minutes}m`
  return `${minutes}m`
}

// Build the list of relevant items — anything live now, plus anything
// still upcoming — sorted so live sales lead, upcoming ones follow in order.
function getItems(now) {
  return SALE_EVENTS
    .map(e => {
      const start = new Date(e.start)
      const end = new Date(e.end)
      if (now >= start && now <= end) return { ...e, type: 'live', msUntil: end - now }
      if (now < start) return { ...e, type: 'upcoming', msUntil: start - now }
      return null
    })
    .filter(Boolean)
    .sort((a, b) => (a.type === b.type ? 0 : a.type === 'live' ? -1 : 1))
}

export default function SaleTicker() {
  const [items, setItems] = useState(() => getItems(new Date()))

  useEffect(() => {
    const id = setInterval(() => setItems(getItems(new Date())), 60000)
    return () => clearInterval(id)
  }, [])

  if (items.length === 0) return null

  // duplicate so the CSS translate loop is seamless
  const loop = [...items, ...items]

  return (
    <div style={{
      minWidth: 0,
      maxWidth: 420,
      overflow: 'hidden',
      maskImage: 'linear-gradient(90deg, transparent, #000 8%, #000 92%, transparent)',
      WebkitMaskImage: 'linear-gradient(90deg, transparent, #000 8%, #000 92%, transparent)',
    }}>
      <div style={{
        display: 'flex',
        width: 'max-content',
        gap: 12,
        animation: `saleScroll ${items.length * 9}s linear infinite`,
      }}>
        {loop.map((item, i) => <SalePill key={i} item={item} />)}
      </div>

      <style>{`
        @keyframes saleScroll {
          from { transform: translateX(0); }
          to { transform: translateX(-50%); }
        }
        @keyframes saleLivePulse {
          0% { transform: scale(1); opacity: 0.7; }
          100% { transform: scale(2.6); opacity: 0; }
        }
      `}</style>
    </div>
  )
}

function SalePill({ item }) {
  const isLive = item.type === 'live'
  return (
    <a
      href={AMAZON_URL}
      target="_blank"
      rel="noopener noreferrer"
      style={{
        display: 'flex',
        alignItems: 'center',
        gap: 10,
        background: isLive ? 'rgba(239,68,68,0.08)' : 'var(--bg3)',
        border: `1px solid ${isLive ? 'rgba(239,68,68,0.3)' : 'var(--border-strong)'}`,
        borderRadius: 20,
        padding: '6px 14px 6px 10px',
        flexShrink: 0,
        whiteSpace: 'nowrap',
      }}
    >
      {isLive ? (
        <span style={{ position: 'relative', width: 8, height: 8, flexShrink: 0 }}>
          <span style={{
            position: 'absolute', inset: 0, borderRadius: '50%',
            background: '#ef4444', animation: 'saleLivePulse 1.6s ease-out infinite',
          }} />
          <span style={{ position: 'absolute', inset: 0, borderRadius: '50%', background: '#ef4444' }} />
        </span>
      ) : (
        <span style={{ fontSize: 13 }}>🛍️</span>
      )}

      <span style={{
        fontSize: 11, fontWeight: 700, letterSpacing: '0.4px', textTransform: 'uppercase',
        color: isLive ? '#dc2626' : 'var(--text-dim)',
      }}>
        {isLive ? 'Live' : 'Upcoming'}
      </span>

      <span style={{ fontSize: 13, fontWeight: 600, color: 'var(--ink)' }}>
        Amazon {item.name}
      </span>

      <span style={{ fontSize: 12, color: 'var(--text-muted)' }}>
        {isLive ? `ends in ${formatDuration(item.msUntil)}` : `in ${formatDuration(item.msUntil)}`}
      </span>
    </a>
  )
}
