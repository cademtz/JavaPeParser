#include <Windows.h>
#pragma comment(lib, "user32.lib")

int mainCRTStartup() {
	MessageBoxA(0, "Hello from C", "Hello world", MB_OK);
	return 0;
}

int main() {
	return 0;
}