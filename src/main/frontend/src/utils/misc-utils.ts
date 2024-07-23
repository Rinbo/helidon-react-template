export function isSingleDigit(string: string): boolean {
  return string.length === 1 && string.match(/[0-9]/) !== null;
}

export function isEmpty<T>(array: T[] | undefined | null): boolean {
  if (!array) return true;
  return array.length === 0;
}
