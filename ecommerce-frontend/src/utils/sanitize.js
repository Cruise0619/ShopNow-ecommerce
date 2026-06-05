const DANGEROUS_TAGS = /<\/?(script|iframe|embed|object|form|input|button|select|textarea|link|meta|style|base|applet|frame|frameset|ilayer|layer|bgsound|title|head)(\s[^>]*)?>/gi
const EVENT_ATTRS = /\s+on\w+\s*=\s*("[^"]*"|'[^']*'|[^\s>]*)/gi
const JAVASCRIPT_URL = /\s+(?:href|src|action|formaction|data)\s*=\s*"[^"]*javascript:/gi

export function sanitizeHTML(html) {
  if (!html || typeof html !== 'string') return ''
  return html
    .replace(DANGEROUS_TAGS, '')
    .replace(EVENT_ATTRS, '')
    .replace(JAVASCRIPT_URL, '')
}
