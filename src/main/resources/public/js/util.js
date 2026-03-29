function formatUnit(bytes, sizes, netLine) {
    if (bytes === 0) {
        return netLine ? `0\n${sizes[0]}` : `0 ${sizes[0]}`;
    }

    const k = 1024;
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    const value = parseFloat((bytes / Math.pow(k, i)).toFixed(2));

    return netLine ? `${value}\n${sizes[i]}` : `${value} ${sizes[i]}`;
}

export function formatNet(bytes, netLine = false) {
    const sizes = ['B/s', 'KiB/s', 'MiB/s', 'GiB/s', 'TiB/s', 'PiB/s'];
    return formatUnit(bytes, sizes, netLine);
}

export function formatByte(bytes, netLine = false) {
    const sizes = ['Byte', 'KiB', 'MiB', 'GiB', 'TiB', 'PiB'];
    return formatUnit(bytes, sizes, netLine);
}

export function formatValue(value, format, netLine = false) {
    if (value === null || value === undefined || value === "") {
        return "N/A";
    }
    if (typeof value !== "number" || isNaN(value)) {
        return String(value);
    }
    switch (format) {
        case 'int':
            return Math.round(value) + `\u200B`;
        case 'percent':
            return (value * 100).toFixed(0) + '%';
        case 'float':
            return value.toFixed(2);
        case 'net':
            return formatNet(value, netLine);
        case 'byte':
            return formatByte(value, netLine);
        default:
            return value;
    }
}