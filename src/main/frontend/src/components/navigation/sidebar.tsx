export default function SideBar() {
  return (
    <nav className="left 0 sticky top-1/2 z-20 mx-1 hidden w-fit translate-y-[-50%] rounded px-2 py-4 sm:block">
      <div className="flex flex-col gap-4">
        <div className="h-12 w-12 rounded bg-secondary text-center align-middle">1</div>
        <div className="h-12 w-12 rounded bg-secondary text-center">2</div>
        <div className="h-12 w-12 rounded bg-secondary text-center">3</div>
      </div>
    </nav>
  );
}
