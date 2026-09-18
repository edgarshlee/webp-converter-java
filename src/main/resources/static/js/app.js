const form = document.querySelector('#converter');
const input = document.querySelector('#files');
const dropZone = document.querySelector('#drop-zone');
const fileList = document.querySelector('#file-list');
const quality = document.querySelector('#quality');
const qualityValue = document.querySelector('#quality-value');
const options = document.querySelector('#conversion-options');
const jpgQualityOption = document.querySelector('#jpg-quality-option');
const jpgBackgroundOption = document.querySelector('#jpg-background-option');
const submit = document.querySelector('#submit');
const status = document.querySelector('#status');

quality.addEventListener('input', () => {
    qualityValue.value = quality.value;
});

form.querySelectorAll('input[name="format"]').forEach(radio => {
    radio.addEventListener('change', updateFormatOptions);
});

updateFormatOptions();

for (const eventName of ['dragenter', 'dragover']) {
    dropZone.addEventListener(eventName, event => {
        event.preventDefault();
        dropZone.classList.add('dragging');
    });
}

for (const eventName of ['dragleave', 'drop']) {
    dropZone.addEventListener(eventName, event => {
        event.preventDefault();
        dropZone.classList.remove('dragging');
    });
}

dropZone.addEventListener('drop', event => {
    input.files = event.dataTransfer.files;
    renderFiles();
});

input.addEventListener('change', renderFiles);

form.addEventListener('submit', async event => {
    event.preventDefault();

    if (!input.files.length) {
        return;
    }

    submit.disabled = true;
    status.className = 'status';
    status.textContent = '이미지를 변환하고 있습니다…';

    try {
        const response = await fetch('/api/conversions', {
            method: 'POST',
            body: new FormData(form),
        });

        if (!response.ok) {
            const problem = await response.json().catch(() => ({}));
            throw new Error(problem.detail || '변환 요청을 처리하지 못했습니다.');
        }

        const blob = await response.blob();
        const disposition = response.headers.get('Content-Disposition') || '';
        const matchedFilename = disposition.match(/filename="?([^";]+)"?/i);
        const downloadUrl = URL.createObjectURL(blob);
        const downloadLink = document.createElement('a');

        downloadLink.href = downloadUrl;
        downloadLink.download = matchedFilename?.[1] || 'webp-converted.zip';
        downloadLink.click();
        URL.revokeObjectURL(downloadUrl);

        status.textContent = '변환이 완료되어 ZIP 파일을 다운로드했습니다.';
    } catch (error) {
        status.className = 'status error';
        status.textContent = error.message;
    } finally {
        submit.disabled = false;
    }
});

function updateFormatOptions() {
    const isJpg = form.elements.format.value === 'JPG';

    jpgQualityOption.hidden = !isJpg;
    jpgBackgroundOption.hidden = !isJpg;
    options.classList.toggle('png-mode', !isJpg);
}

function renderFiles() {
    const fileItems = Array.from(input.files).map((file, index) => {
        const item = document.createElement('div');
        const details = document.createElement('div');
        const name = document.createElement('strong');
        const size = document.createElement('span');
        const removeButton = document.createElement('button');

        item.className = 'file-item';
        details.className = 'file-details';
        name.textContent = file.name;
        size.textContent = formatBytes(file.size);
        removeButton.type = 'button';
        removeButton.className = 'remove-file';
        removeButton.textContent = '삭제';
        removeButton.setAttribute('aria-label', `${file.name} 삭제`);
        removeButton.addEventListener('click', () => removeFile(index));

        details.append(name, size);
        item.append(details, removeButton);
        return item;
    });

    fileList.replaceChildren(...fileItems);
    status.className = 'status';
    status.textContent = input.files.length
        ? `${input.files.length}개 파일을 선택했습니다.`
        : '파일을 선택하면 변환을 시작할 수 있습니다.';
}

function removeFile(index) {
    const transfer = new DataTransfer();

    Array.from(input.files).forEach((file, currentIndex) => {
        if (currentIndex !== index) {
            transfer.items.add(file);
        }
    });

    input.files = transfer.files;
    renderFiles();
}

function formatBytes(bytes) {
    if (bytes < 1024) {
        return `${bytes} B`;
    }

    if (bytes < 1024 ** 2) {
        return `${(bytes / 1024).toFixed(1)} KB`;
    }

    return `${(bytes / 1024 ** 2).toFixed(1)} MB`;
}
