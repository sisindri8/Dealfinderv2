import React, { useState } from 'react'

const CATEGORY_COLORS = {
  'Best Overall':    { bg: 'rgba(245,158,11,0.12)', border: 'rgba(245,158,11,0.35)', text: '#b45309' },
  "Editor's Choice": { bg: 'rgba(139,92,246,0.12)', border: 'rgba(139,92,246,0.35)', text: '#7c3aed' },
  'Best Value':      { bg: 'rgba(16,185,129,0.12)', border: 'rgba(16,185,129,0.35)', text: '#0f9d70' },
  'Most Popular':    { bg: 'rgba(239,68,68,0.12)',  border: 'rgba(239,68,68,0.35)',  text: '#dc2626' },
  'Budget Pick':     { bg: 'rgba(79,107,255,0.12)', border: 'rgba(79,107,255,0.35)', text: '#4f6bff' },
}

export default function ProductCard({ product, featured = false, isFavorite = false, onToggleFavorite }) {
  const [imgError, setImgError] = useState(false)
  const colors = CATEGORY_COLORS[product.category] || CATEGORY_COLORS['Best Overall']

  const favoriteProps = { isFavorite, onToggleFavorite }

  if (featured) return <FeaturedCard product={product} colors={colors} imgError={imgError} setImgError={setImgError} {...favoriteProps} />
  return <CompactCard product={product} colors={colors} imgError={imgError} setImgError={setImgError} {...favoriteProps} />
}

function FavoriteToggle({ isFavorite, onToggleFavorite }) {
  if (!onToggleFavorite) return null
  return (
    <button
      onClick={(e) => { e.preventDefault(); e.stopPropagation(); onToggleFavorite() }}
      title={isFavorite ? 'Remove from favorites' : 'Save to favorites'}
      style={{
        width: 32, height: 32, borderRadius: '50%',
        background: 'rgba(255,255,255,0.92)',
        border: '1px solid var(--border)',
        display: 'flex', alignItems: 'center', justifyContent: 'center',
        cursor: 'pointer', fontSize: 15, lineHeight: 1,
        boxShadow: '0 1px 4px rgba(16,24,40,0.12)',
      }}
    >
      {isFavorite ? '❤️' : '🤍'}
    </button>
  )
}

function ImagePlaceholder({ size = 48 }) {
  return (
    <div style={{
      width: size * 1.6, height: size * 1.6,
      borderRadius: size * 0.4,
      background: 'linear-gradient(135deg, #eef0f7, #e3e6f0)',
      display: 'flex', alignItems: 'center', justifyContent: 'center',
      color: '#b7bccb', fontSize: size,
    }}>📦</div>
  )
}

function FeaturedCard({ product, colors, imgError, setImgError, isFavorite, onToggleFavorite }) {
  return (
    <div style={{
      background: 'var(--bg2)',
      border: '1px solid var(--border)',
      borderRadius: 'var(--radius)',
      overflow: 'hidden',
      display: 'grid',
      gridTemplateColumns: '300px 1fr',
      marginBottom: 18,
      position: 'relative',
      boxShadow: 'var(--shadow-lg)',
    }}>
      <div style={{ position: 'absolute', top: 14, right: 14, zIndex: 2 }}>
        <FavoriteToggle isFavorite={isFavorite} onToggleFavorite={onToggleFavorite} />
      </div>
      {/* Image */}
      <div style={{
        background: 'var(--bg3)',
        display: 'flex', alignItems: 'center', justifyContent: 'center',
        minHeight: 260, overflow: 'hidden',
      }}>
        {product.imageUrl && !imgError ? (
          <img src={product.imageUrl} alt={product.productName}
            style={{ width: '100%', height: '100%', objectFit: 'contain', padding: 20 }}
            onError={() => setImgError(true)} />
        ) : (
          <ImagePlaceholder size={44} />
        )}
      </div>

      {/* Content */}
      <div style={{ padding: 30, display: 'flex', flexDirection: 'column', gap: 14, position: 'relative' }}>
        <CategoryBadge product={product} colors={colors} />
        <h2 style={{ fontSize: 22, fontWeight: 700, letterSpacing: '-0.3px', lineHeight: 1.3, color: 'var(--ink)' }}>
          {product.productName}
        </h2>
        {product.mentionCount > 1 && (
          <div style={{ fontSize: 13, color: 'var(--text-muted)' }}>
            ✓ Recommended by <strong style={{ color: 'var(--ink)' }}>{product.mentionCount} YouTube reviewers</strong>
          </div>
        )}
        <VideoSource product={product} />
        <BuyButton url={product.affiliateUrl} large />
      </div>
    </div>
  )
}

function CompactCard({ product, colors, imgError, setImgError, isFavorite, onToggleFavorite }) {
  return (
    <div style={{
      background: 'var(--bg2)',
      border: '1px solid var(--border)',
      borderRadius: 'var(--radius)',
      overflow: 'hidden',
      display: 'flex',
      flexDirection: 'column',
      boxShadow: 'var(--shadow-md)',
      transition: 'box-shadow 0.2s, transform 0.2s',
    }}
      onMouseEnter={e => { e.currentTarget.style.boxShadow = 'var(--shadow-lg)'; e.currentTarget.style.transform = 'translateY(-3px)' }}
      onMouseLeave={e => { e.currentTarget.style.boxShadow = 'var(--shadow-md)'; e.currentTarget.style.transform = 'none' }}
    >
      {/* Image */}
      <div style={{ height: 180, background: 'var(--bg3)', display: 'flex', alignItems: 'center', justifyContent: 'center', overflow: 'hidden', position: 'relative' }}>
        {product.imageUrl && !imgError ? (
          <img src={product.imageUrl} alt={product.productName}
            style={{ width: '100%', height: '100%', objectFit: 'contain', padding: 14 }}
            onError={() => setImgError(true)} />
        ) : (
          <ImagePlaceholder size={30} />
        )}
        <div style={{ position: 'absolute', top: 10, left: 10 }}>
          <CategoryBadge product={product} colors={colors} />
        </div>
        <div style={{ position: 'absolute', top: 10, right: 10 }}>
          <FavoriteToggle isFavorite={isFavorite} onToggleFavorite={onToggleFavorite} />
        </div>
      </div>

      {/* Body */}
      <div style={{ padding: 16, flex: 1, display: 'flex', flexDirection: 'column', gap: 10 }}>
        <div style={{ fontSize: 14, fontWeight: 600, lineHeight: 1.4, color: 'var(--ink)' }}>
          {product.productName}
        </div>
        {product.mentionCount > 1 && (
          <div style={{ fontSize: 11, fontWeight: 600, color: colors.text }}>
            ✓ {product.mentionCount} reviewers recommended
          </div>
        )}
        <VideoSource product={product} compact />
        <BuyButton url={product.affiliateUrl} />
      </div>
    </div>
  )
}

function CategoryBadge({ product, colors }) {
  return (
    <div style={{
      display: 'inline-flex', alignItems: 'center', gap: 5,
      background: colors.bg,
      border: `1px solid ${colors.border}`,
      color: colors.text,
      fontSize: 11, fontWeight: 700,
      padding: '4px 11px', borderRadius: 20,
      textTransform: 'uppercase', letterSpacing: '0.4px',
      width: 'fit-content',
      boxShadow: '0 1px 3px rgba(16,24,40,0.06)',
    }}>
      <span>{product.categoryEmoji}</span>
      <span>{product.category}</span>
    </div>
  )
}

function VideoSource({ product, compact }) {
  return (
    <a href={product.videoUrl} target="_blank" rel="noopener noreferrer"
      style={{ display: 'flex', alignItems: 'center', gap: 8, marginTop: 'auto' }}>
      {product.videoThumbnailUrl && (
        <img src={product.videoThumbnailUrl} alt=""
          style={{ width: compact ? 42 : 52, height: compact ? 30 : 36, borderRadius: 6, objectFit: 'cover', flexShrink: 0 }} />
      )}
      <div style={{ overflow: 'hidden' }}>
        <div style={{ fontSize: 11, color: 'var(--text-dim)', marginBottom: 2 }}>{product.channelName}</div>
        <div style={{
          fontSize: 11, color: 'var(--text-muted)',
          overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap',
          maxWidth: compact ? 160 : 300,
        }}>{product.videoTitle}</div>
      </div>
    </a>
  )
}

function BuyButton({ url, large }) {
  return (
    <a href={url} target="_blank" rel="noopener noreferrer" style={{
      display: 'block', textAlign: 'center', marginTop: large ? 8 : 0,
      background: 'var(--ink)',
      color: '#fff',
      fontWeight: 600,
      fontSize: large ? 15 : 13,
      padding: large ? '13px 20px' : '10px 16px',
      borderRadius: large ? 12 : 10,
      transition: 'opacity 0.15s',
    }}
      onMouseEnter={e => e.currentTarget.style.opacity = '0.85'}
      onMouseLeave={e => e.currentTarget.style.opacity = '1'}
    >
      🛒 Buy on Amazon
    </a>
  )
}
