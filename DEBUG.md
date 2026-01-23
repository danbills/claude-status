# Statusline Debug Logging

The wrapper script now logs all execution details to `/tmp/statusline-debug/`.

## Log Files

Each invocation creates 4 files with a timestamp:

1. **`run-*.log`** - Main debug log with:
   - Timestamp
   - Arguments passed
   - Working directory
   - Stdin size and preview
   - Exit code
   - Output/error sizes
   - Stderr content (if any)

2. **`stdin-*.json`** - Complete stdin input (the JSON from Claude Code)

3. **`output-*.txt`** - Complete stdout output (the statusline)

4. **`error-*.txt`** - Complete stderr output (errors/warnings)

## How to Debug

### After running in Claude Code:

```bash
# View the latest run log
cat /tmp/statusline-debug/run-*.log | tail -50

# View the latest stdin (what Claude Code sent)
cat /tmp/statusline-debug/stdin-*.json | tail -1 | jq

# View the latest output (what we returned)
cat /tmp/statusline-debug/output-*.txt | tail -1

# View the latest errors
cat /tmp/statusline-debug/error-*.txt | tail -1
```

### Check if the script is being called:

```bash
# List all recent runs (sorted by time)
ls -lt /tmp/statusline-debug/run-*.log | head -5
```

### Monitor in real-time:

```bash
# Watch the debug directory for new files
watch -n 1 'ls -lt /tmp/statusline-debug/ | head -10'
```

## Cleanup

The script automatically keeps only the 10 most recent log sets. Older logs are automatically deleted.

## What to Look For

1. **Script not called at all?**
   - No new files in `/tmp/statusline-debug/`
   - Check your Claude Code hook configuration

2. **Script called but no stdin?**
   - `stdin-*.json` is empty or very small
   - Claude Code may not be sending the JSON

3. **Script called with stdin but no output?**
   - Check `error-*.txt` for errors
   - Check exit code in `run-*.log`

4. **Output generated but not displayed?**
   - Check `output-*.txt` - the statusline is there
   - Issue is with Claude Code displaying the output
