import React, { useState } from 'react'
import homeHero from '../assets/home-hero.png'

const CHANNELS = [
  { name: 'Mrwhosetheboss', color: '#ef4444' },
  { name: 'Gerry Dawes', color: '#f59e0b' },
  { name: 'ShortCircuit', color: '#10b981' },
  { name: 'Linus Tech Tips', color: '#4f6bff' },
  { name: 'Marques Brownlee', color: '#8b5cf6' },
  { name: 'Dave2D', color: '#ec4899' },
  { name: 'JayzTwoCents', color: '#0ea5e9' },
  { name: 'Hardware Unboxed', color: '#f97316' },
  { name: 'Techquickie', color: '#14b8a6' },
  { name: 'Unbox Therapy', color: '#a855f7' },
]

function initials(name) {
  return name
    .split(' ')
    .map(w => w[0])
    .join('')
    .slice(0, 2)
    .toUpperCase()
}

export default function Footer() {
  return (
    <footer style={{ background: 'var(--bg2)', borderTop: '1px solid var(--border)', marginTop: 40 }}>
      <ChannelLogoScroll />
      <AboutSection />
      <ContactSection />
      <BottomBar />
    </footer>
  )
}

/* ---------------- YouTube channel logos — infinite scroll ---------------- */

function ChannelLogoScroll() {
  const loop = [...CHANNELS, ...CHANNELS]

  return (
    <div style={{ padding: '36px 0 32px', overflow: 'hidden', borderBottom: '1px solid var(--border)' }}>
      <p style={{
        textAlign: 'center',
        fontSize: 12,
        fontWeight: 600,
        letterSpacing: '0.4px',
        textTransform: 'uppercase',
        color: 'var(--text-dim)',
        marginBottom: 24,
      }}>Trusted YouTube channels we scan</p>

      <div style={{
        display: 'flex',
        width: 'max-content',
        gap: 44,
        animation: 'logoScroll 32s linear infinite',
      }}>
        {loop.map((ch, i) => (
          <div key={i} style={{
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
            gap: 8,
            width: 92,
            flexShrink: 0,
          }}>
            <div style={{ position: 'relative' }}>
              <div style={{
                width: 52, height: 52, borderRadius: '50%',
                background: `linear-gradient(135deg, ${ch.color}, ${ch.color}cc)`,
                display: 'flex', alignItems: 'center', justifyContent: 'center',
                color: '#fff', fontWeight: 700, fontSize: 15,
                boxShadow: 'var(--shadow-md)',
              }}>
                {initials(ch.name)}
              </div>
              {/* YouTube play badge */}
              <div style={{
                position: 'absolute', bottom: -3, right: -3,
                width: 22, height: 16, borderRadius: 5,
                background: '#FF0000',
                display: 'flex', alignItems: 'center', justifyContent: 'center',
                border: '2px solid var(--bg2)',
              }}>
                <div style={{
                  width: 0, height: 0,
                  borderTop: '4px solid transparent',
                  borderBottom: '4px solid transparent',
                  borderLeft: '6px solid #fff',
                  marginLeft: 2,
                }} />
              </div>
            </div>
            <span style={{
              fontSize: 11.5,
              fontWeight: 600,
              color: 'var(--text-muted)',
              textAlign: 'center',
              lineHeight: 1.3,
            }}>{ch.name}</span>
          </div>
        ))}
      </div>

      <style>{`
        @keyframes logoScroll {
          from { transform: translateX(0); }
          to { transform: translateX(-50%); }
        }
      `}</style>
    </div>
  )
}

/* ---------------- About ---------------- */

function AboutSection() {
  return (
    <div id="about" style={{
      maxWidth: 1100, margin: '0 auto', padding: '56px 24px',
      display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 48,
      alignItems: 'center', borderBottom: '1px solid var(--border)',
    }}>
      <div style={{
        borderRadius: 'var(--radius)', overflow: 'hidden',
        boxShadow: 'var(--shadow-lg)', border: '1px solid var(--border)',
      }}>
        <img src={homeHero} alt="DealFinder — redesigning how you discover trusted picks"
          style={{ width: '100%', height: '100%', objectFit: 'cover', display: 'block' }} />
      </div>

      <div>
        <span style={{
          fontSize: 12, fontWeight: 700, letterSpacing: '0.6px', textTransform: 'uppercase',
          color: 'var(--accent)', background: 'var(--accent-dim)',
          padding: '5px 12px', borderRadius: 20,
        }}>About Us</span>

        <h2 style={{ fontSize: 28, fontWeight: 700, letterSpacing: '-0.4px', color: 'var(--ink)', margin: '16px 0 14px' }}>
          Redesigning how you discover trusted picks
        </h2>

        <p style={{ color: 'var(--text-muted)', fontSize: 15, lineHeight: 1.7, marginBottom: 14 }}>
          DealFinder scans reviews from the tech YouTubers people already trust, pulls out the
          products they actually recommend, and ranks them by how many reviewers agree — so you
          don't have to watch a dozen videos to make one decision.
        </p>
        <p style={{ color: 'var(--text-muted)', fontSize: 15, lineHeight: 1.7 }}>
          Every pick links straight to the product and the video it came from, giving you the
          full context behind the recommendation — not just a name on a list.
        </p>
      </div>
    </div>
  )
}

/* ---------------- Contact ---------------- */

function ContactSection() {
  const [form, setForm] = useState({ name: '', email: '', message: '' })
  const [sent, setSent] = useState(false)
  const [copied, setCopied] = useState(false)

  const handleChange = e => setForm(f => ({ ...f, [e.target.name]: e.target.value }))

  const handleSubmit = e => {
    e.preventDefault()
    const subject = encodeURIComponent(`Message from ${form.name || 'DealFinder visitor'}`)
    const body = encodeURIComponent(`${form.message}\n\n— ${form.name} (${form.email})`)
    window.location.href = `mailto:sisindri.dev@gmail.com?subject=${subject}&body=${body}`
    setSent(true)
  }

  const handleCopyEmail = async () => {
    try {
      await navigator.clipboard.writeText('sisindri.dev@gmail.com')
      setCopied(true)
      setTimeout(() => setCopied(false), 2000)
    } catch {
      /* clipboard API unavailable — ignore */
    }
  }

  const inputStyle = {
    width: '100%',
    background: 'var(--bg3)',
    border: '1px solid var(--border-strong)',
    borderRadius: 10,
    padding: '11px 14px',
    fontSize: 14,
    fontFamily: 'var(--font)',
    color: 'var(--text)',
    outline: 'none',
  }

  return (
    <div id="contact" style={{ maxWidth: 1100, margin: '0 auto', padding: '56px 24px' }}>
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1.2fr', gap: 48 }}>
        <div>
          <span style={{
            fontSize: 12, fontWeight: 700, letterSpacing: '0.6px', textTransform: 'uppercase',
            color: 'var(--accent)', background: 'var(--accent-dim)',
            padding: '5px 12px', borderRadius: 20,
          }}>Contact Us</span>

          <h2 style={{ fontSize: 26, fontWeight: 700, letterSpacing: '-0.4px', color: 'var(--ink)', margin: '16px 0 14px' }}>
            Questions, feedback, or a channel we should scan?
          </h2>
          <p style={{ color: 'var(--text-muted)', fontSize: 14.5, lineHeight: 1.7, marginBottom: 20 }}>
            We'd love to hear from you. Reach out any time and we'll get back to you shortly.
          </p>

          <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
            <a href="mailto:sisindri.dev@gmail.com" style={{ display: 'flex', alignItems: 'center', gap: 10, fontSize: 14, color: 'var(--text)', fontWeight: 500 }}>
              <span style={{ fontSize: 16 }}>✉️</span> sisindri.dev@gmail.com
            </a>
            <a href="https://www.linkedin.com/in/kasukurthi-sisindri/" target="_blank" rel="noopener noreferrer"
              style={{ display: 'flex', alignItems: 'center', gap: 10, fontSize: 14, color: 'var(--text)', fontWeight: 500 }}>
              <span style={{
                width: 26, height: 26, borderRadius: 6, background: '#0A66C2',
                color: '#fff', display: 'flex', alignItems: 'center', justifyContent: 'center',
                fontSize: 13, fontWeight: 700, flexShrink: 0,
              }}>in</span>
              LinkedIn
            </a>
          </div>
        </div>

        <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
          <div style={{ display: 'flex', gap: 12 }}>
            <input name="name" required placeholder="Your name" value={form.name} onChange={handleChange} style={inputStyle} />
            <input name="email" type="email" required placeholder="Your email" value={form.email} onChange={handleChange} style={inputStyle} />
          </div>
          <textarea name="message" required placeholder="Your message" rows={5} value={form.message} onChange={handleChange}
            style={{ ...inputStyle, resize: 'vertical', fontFamily: 'var(--font)' }} />
          <div style={{ display: 'flex', alignItems: 'center', gap: 14, flexWrap: 'wrap' }}>
            <button type="submit" style={{
              background: 'var(--ink)', color: '#fff', fontWeight: 600, fontSize: 14,
              border: 'none', borderRadius: 10, padding: '12px 20px', cursor: 'pointer',
            }}>
              {sent ? 'Opening your mail app…' : 'Send message'}
            </button>
            <button type="button" onClick={handleCopyEmail} style={{
              background: 'transparent', color: 'var(--text-muted)', fontWeight: 500, fontSize: 13,
              border: 'none', cursor: 'pointer', textDecoration: 'underline',
            }}>
              {copied ? 'Email copied ✓' : "Mail app not opening? Copy email instead"}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}

/* ---------------- Bottom bar ---------------- */

function BottomBar() {
  return (
    <div style={{
      borderTop: '1px solid var(--border)',
      padding: '20px 24px',
      textAlign: 'center',
      fontSize: 12.5,
      color: 'var(--text-dim)',
    }}>
      © {new Date().getFullYear()} DealFinder · Product links may be Amazon affiliate links
    </div>
  )
}
