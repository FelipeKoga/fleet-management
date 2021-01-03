import { format, toDate } from "date-fns";

export function convertDate(createdAt: number, dateFormat: string) {
  if (!createdAt) return;
  return format(toDate(createdAt), dateFormat);
}
