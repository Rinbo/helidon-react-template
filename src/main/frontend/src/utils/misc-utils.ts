export function isSingleDigit(string: string): boolean {
  return string.length === 1 && string.match(/[0-9]/) !== null;
}

export function isEmpty<T>(array: T[] | undefined | null): boolean {
  if (!array) return true;
  return array.length === 0;
}

export async function sha256(string: string) {
  const encoder = new TextEncoder();
  const data = encoder.encode(string);

  const hashBuffer = await crypto.subtle.digest("SHA-256", data);

  const hashArray = Array.from(new Uint8Array(hashBuffer));
  return hashArray.map((b) => b.toString(16).padStart(2, "0")).join("");
}
