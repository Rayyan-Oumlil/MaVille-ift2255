"use client"

import { useState, useRef, useCallback } from "react"
import { Button } from "@/components/ui/button"
import { Card } from "@/components/ui/card"
import { X, Upload, Image as ImageIcon, File } from "lucide-react"
import { cn } from "@/lib/utils"

export interface UploadedFile {
  file: File
  preview?: string
  id: string
}

interface FileUploadProps {
  files: UploadedFile[]
  onFilesChange: (files: UploadedFile[]) => void
  accept?: string
  maxFiles?: number
  maxSizeMB?: number
  className?: string
  disabled?: boolean
}

export function FileUpload({
  files,
  onFilesChange,
  accept = "image/*",
  maxFiles = 5,
  maxSizeMB = 5,
  className,
  disabled = false,
}: FileUploadProps) {
  const [isDragging, setIsDragging] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const fileInputRef = useRef<HTMLInputElement>(null)

  const validateFile = (file: File): string | null => {
    // Vérifier la taille
    const maxSizeBytes = maxSizeMB * 1024 * 1024
    if (file.size > maxSizeBytes) {
      return `Le fichier "${file.name}" dépasse la taille maximale de ${maxSizeMB}MB`
    }

    // Vérifier le type (si accept est spécifié)
    if (accept && accept !== "*") {
      const acceptedTypes = accept.split(",").map((t) => t.trim())
      const isAccepted = acceptedTypes.some((type) => {
        if (type.endsWith("/*")) {
          return file.type.startsWith(type.slice(0, -1))
        }
        return file.type === type
      })

      if (!isAccepted) {
        return `Le type de fichier "${file.type}" n'est pas accepté`
      }
    }

    return null
  }

  const processFiles = useCallback(
    (fileList: FileList | File[]) => {
      const newFiles: UploadedFile[] = []
      const fileArray = Array.from(fileList)

      // Vérifier le nombre maximum de fichiers
      if (files.length + fileArray.length > maxFiles) {
        setError(`Vous ne pouvez pas ajouter plus de ${maxFiles} fichiers`)
        return
      }

      for (const file of fileArray) {
        const validationError = validateFile(file)
        if (validationError) {
          setError(validationError)
          continue
        }

        const id = `${Date.now()}-${Math.random().toString(36).substr(2, 9)}`
        const uploadedFile: UploadedFile = { file, id }

        // Créer une prévisualisation pour les images
        if (file.type.startsWith("image/")) {
          const reader = new FileReader()
          reader.onload = (e) => {
            uploadedFile.preview = e.target?.result as string
            onFilesChange([...files, ...newFiles, uploadedFile])
          }
          reader.readAsDataURL(file)
        } else {
          newFiles.push(uploadedFile)
        }
      }

      if (newFiles.length > 0) {
        onFilesChange([...files, ...newFiles])
      }
    },
    [files, maxFiles, onFilesChange]
  )

  const handleFileSelect = (e: React.ChangeEvent<HTMLInputElement>) => {
    setError(null)
    if (e.target.files && e.target.files.length > 0) {
      processFiles(e.target.files)
    }
    // Réinitialiser l'input pour permettre de sélectionner le même fichier
    if (fileInputRef.current) {
      fileInputRef.current.value = ""
    }
  }

  const handleDragOver = (e: React.DragEvent) => {
    e.preventDefault()
    e.stopPropagation()
    if (!disabled) {
      setIsDragging(true)
    }
  }

  const handleDragLeave = (e: React.DragEvent) => {
    e.preventDefault()
    e.stopPropagation()
    setIsDragging(false)
  }

  const handleDrop = (e: React.DragEvent) => {
    e.preventDefault()
    e.stopPropagation()
    setIsDragging(false)
    setError(null)

    if (disabled) return

    if (e.dataTransfer.files && e.dataTransfer.files.length > 0) {
      processFiles(e.dataTransfer.files)
    }
  }

  const removeFile = (id: string) => {
    onFilesChange(files.filter((f) => f.id !== id))
    setError(null)
  }

  const openFileDialog = () => {
    fileInputRef.current?.click()
  }

  return (
    <div className={cn("space-y-4", className)}>
      {/* Zone de drop */}
      <Card
        className={cn(
          "border-2 border-dashed transition-colors",
          isDragging && !disabled
            ? "border-primary bg-primary/5"
            : "border-muted-foreground/25",
          disabled && "opacity-50 cursor-not-allowed"
        )}
        onDragOver={handleDragOver}
        onDragLeave={handleDragLeave}
        onDrop={handleDrop}
      >
        <div className="p-6 text-center">
          <Upload
            className={cn(
              "mx-auto h-12 w-12 mb-4",
              isDragging ? "text-primary" : "text-muted-foreground"
            )}
          />
          <p className="text-sm font-medium mb-2">
            Glissez-déposez vos fichiers ici
          </p>
          <p className="text-xs text-muted-foreground mb-4">
            ou cliquez pour sélectionner des fichiers
          </p>
          <Button
            type="button"
            variant="outline"
            size="sm"
            onClick={openFileDialog}
            disabled={disabled || files.length >= maxFiles}
          >
            <Upload className="h-4 w-4 mr-2" />
            Sélectionner des fichiers
          </Button>
          <p className="text-xs text-muted-foreground mt-2">
            Maximum {maxFiles} fichiers, {maxSizeMB}MB par fichier
          </p>
        </div>
      </Card>

      <input
        ref={fileInputRef}
        type="file"
        accept={accept}
        multiple={maxFiles > 1}
        onChange={handleFileSelect}
        className="hidden"
        disabled={disabled}
      />

      {/* Message d'erreur */}
      {error && (
        <div className="p-3 text-sm text-red-500 bg-red-500/10 border border-red-500/20 rounded">
          {error}
        </div>
      )}

      {/* Liste des fichiers */}
      {files.length > 0 && (
        <div className="space-y-2">
          <p className="text-sm font-medium">
            Fichiers sélectionnés ({files.length}/{maxFiles})
          </p>
          <div className="grid grid-cols-2 md:grid-cols-3 gap-4">
            {files.map((uploadedFile) => (
              <Card key={uploadedFile.id} className="relative group">
                <div className="aspect-square relative overflow-hidden rounded-t-lg">
                  {uploadedFile.preview ? (
                    <img
                      src={uploadedFile.preview}
                      alt={uploadedFile.file.name}
                      className="w-full h-full object-cover"
                    />
                  ) : (
                    <div className="w-full h-full flex items-center justify-center bg-muted">
                      <File className="h-8 w-8 text-muted-foreground" />
                    </div>
                  )}
                  <Button
                    type="button"
                    variant="destructive"
                    size="icon"
                    className="absolute top-2 right-2 h-6 w-6 opacity-0 group-hover:opacity-100 transition-opacity"
                    onClick={() => removeFile(uploadedFile.id)}
                    disabled={disabled}
                  >
                    <X className="h-4 w-4" />
                  </Button>
                </div>
                <div className="p-2">
                  <p className="text-xs font-medium truncate" title={uploadedFile.file.name}>
                    {uploadedFile.file.name}
                  </p>
                  <p className="text-xs text-muted-foreground">
                    {(uploadedFile.file.size / 1024 / 1024).toFixed(2)} MB
                  </p>
                </div>
              </Card>
            ))}
          </div>
        </div>
      )}
    </div>
  )
}
