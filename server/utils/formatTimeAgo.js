function formatTimeAgo(dateString) {
  const date = new Date(dateString);
  const now = new Date();
  const diff = Math.floor((now - date) / 1000);
  if (diff < 60) return "just now";
  if (diff < 3600) return `${Math.floor(diff / 60)} min ago`;
  if (diff < 86400) return `${Math.floor(diff / 3600)} h ago`;
  if (diff < 2592000) return `${Math.floor(diff / 86400)} d ago`;
  if (diff < 31536000) return `${Math.floor(diff / 2592000)} mo ago`;
  return `${Math.floor(diff / 31536000)} y ago`;
}

export default formatTimeAgo;
